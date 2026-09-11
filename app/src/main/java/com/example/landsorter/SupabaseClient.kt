package com.example.landsorter

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {
    // TODO: Replace these with your actual Supabase URL and Key
    private const val SUPABASE_URL = "https://zkfaxgjswooptjpsftjz.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InprZmF4Z2pzd29vcHRqcHNmdGp6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODkxMjY3OTMsImV4cCI6MjEwNDcwMjc5M30.sk0CG0JBtsPHtQd8Wq11XFyRNUsfXr8ae5wXOD2U3Dw"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Postgrest)
    }
}
