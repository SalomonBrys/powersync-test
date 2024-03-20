package net.kodein.powerludo.ctrl

import androidx.compose.runtime.Composable
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import org.kodein.di.compose.localDI
import org.kodein.di.instance


object Main {

    sealed interface Intent {
        data object Logout : Intent
    }

    @Composable
    fun Components() : MviViewComponents<Unit, Intent> {

        val client: SupabaseClient by localDI().instance()

        return Mvi(
            firstModel = {}
        ) { emit ->
            OnIntent {
                when (it) {
                    is Intent.Logout -> {
                        client.auth.signOut()
                    }
                }
            }
        }
    }

}