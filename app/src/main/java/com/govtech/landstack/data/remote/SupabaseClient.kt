package com.govtech.landstack.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseConfig {
    // TODO: Replace with actual Supabase URL and Anon Key
    private const val SUPABASE_URL = "https://zkfaxgjswooptjpsftjz.supabase.co"
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InprZmF4Z2pzd29vcHRqcHNmdGp6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODkxMjY3OTMsImV4cCI6MjEwNDcwMjc5M30.sk0CG0JBtsPHtQd8Wq11XFyRNUsfXr8ae5wXOD2U3Dw"

    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Postgrest)
    }
}
