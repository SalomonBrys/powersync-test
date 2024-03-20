package net.kodein.powerludo.business

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest


fun createAppSupabaseClient() =
    createSupabaseClient(
        supabaseUrl = SupabaseUrl,
        supabaseKey = SupabaseKey
    ) {
        install(Auth)
        install(Postgrest)
    }
