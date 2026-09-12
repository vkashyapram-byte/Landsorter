package com.govtech.landstack.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseConfig {
    // TODO: Replace with actual Supabase URL and Anon Key
    private const val SUPABASE_URL = "https://your-supabase-url.supabase.co"
    private const val SUPABASE_ANON_KEY = "your-anon-key"

    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Postgrest)
    }
}
