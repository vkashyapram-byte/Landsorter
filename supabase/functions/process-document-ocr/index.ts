import "jsr:@supabase/functions-js/edge-runtime.d.ts"
import { createClient } from "jsr:@supabase/supabase-js@2"

console.log("process-document-ocr function started")

Deno.serve(async (req) => {
  // Handle CORS
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: { 'Access-Control-Allow-Origin': '*', 'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type' } })
  }

  try {
    const body = await req.json()
    const { document_id } = body

    if (!document_id) {
      return new Response(JSON.stringify({ error: "Missing document_id" }), { status: 400, headers: { 'Content-Type': 'application/json' } })
    }

    const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? ""
    const supabaseAnonKey = Deno.env.get("SUPABASE_ANON_KEY") ?? ""
    const supabaseServiceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? ""
    const ocrApiKey = Deno.env.get("OCR_SPACE_KEY") ?? ""

    // 1. Authenticate caller and fetch document metadata using their own permissions
    const authHeader = req.headers.get('Authorization')
    if (!authHeader) {
      return new Response(JSON.stringify({ error: "Missing Authorization header" }), { status: 401, headers: { 'Content-Type': 'application/json' } })
    }

    const supabaseUserClient = createClient(supabaseUrl, supabaseAnonKey, {
      global: { headers: { Authorization: authHeader } }
    })

    const { data: document, error: fetchError } = await supabaseUserClient
      .from('documents')
      .select('*')
      .eq('id', document_id)
      .single()

    if (fetchError || !document) {
      // If RLS blocks it, or it doesn't exist, we reject with 403 (or 404)
      return new Response(JSON.stringify({ error: "Document not found or access denied" }), { status: 403, headers: { 'Content-Type': 'application/json' } })
    }

    // 2. Escalate to Service Role for storage download and final UPDATE
    const supabaseAdmin = createClient(supabaseUrl, supabaseServiceKey)

    // 2. Download file from Storage
    const { data: fileData, error: downloadError } = await supabaseAdmin
      .storage
      .from('parcel-documents')
      .download(document.file_path)

    if (downloadError || !fileData) {
      return new Response(JSON.stringify({ error: "Failed to download file from storage" }), { status: 500, headers: { 'Content-Type': 'application/json' } })
    }

    // 3. Call OCR.space API
    const formData = new FormData()
    formData.append("apikey", ocrApiKey)
    formData.append("language", "eng")
    formData.append("isOverlayRequired", "false")
    formData.append("detectOrientation", "true")
    formData.append("scale", "true")
    
    // File name needs extension to let OCR.space know the type. Using the file path suffix if possible, or fallback to pdf/jpg
    const extMatch = document.file_path.match(/\.[0-9a-z]+$/i)
    const ext = extMatch ? extMatch[0] : ".pdf"
    formData.append("file", fileData, `upload${ext}`)

    const ocrResponse = await fetch("https://api.ocr.space/parse/image", {
      method: "POST",
      body: formData,
    })

    const ocrResult = await ocrResponse.json()
    
    // Handle both OCR.space error formats: JSON with {error, details} or IsErroredOnProcessing flag
    if (ocrResult && (ocrResult.error || ocrResult.IsErroredOnProcessing)) {
      const errMsg = ocrResult.error || (ocrResult.ErrorMessage ? ocrResult.ErrorMessage.join(', ') : 'Unknown OCR error')
      const errDetails = ocrResult.details || ''
      console.error("OCR.space API Error:", errMsg, errDetails)
      return new Response(JSON.stringify({ error: "OCR Processing Failed", details: errMsg }), { status: 502, headers: { 'Content-Type': 'application/json' } })
    }

    let extractedText = ""
    if (ocrResult && ocrResult.ParsedResults && ocrResult.ParsedResults.length > 0) {
      extractedText = ocrResult.ParsedResults.map((r: any) => r.ParsedText).join("\n")
    }

    // 4. Update the document record on success
    const { error: updateError } = await supabaseAdmin
      .from('documents')
      .update({
        ocr_extracted_text: extractedText,
        ocr_processed_at: new Date().toISOString()
      })
      .eq('id', document_id)

    if (updateError) {
      return new Response(JSON.stringify({ error: "Failed to update document with OCR text" }), { status: 500, headers: { 'Content-Type': 'application/json' } })
    }

    return new Response(
      JSON.stringify({ success: true, document_id, extracted_length: extractedText.length }),
      { headers: { "Content-Type": "application/json", "Access-Control-Allow-Origin": "*" } },
    )
  } catch (err) {
    console.error(err)
    return new Response(JSON.stringify({ error: "Internal Server Error" }), { status: 500, headers: { 'Content-Type': 'application/json' } })
  }
})
