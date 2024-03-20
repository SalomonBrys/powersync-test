package net.kodein.powerludo

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.FadeTransition
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
import net.kodein.powerludo.business.Database
import net.kodein.powerludo.business.createAppSupabaseClient
import net.kodein.powerludo.business.utils.rememberDatabaseDriverFactory
import net.kodein.powerludo.screen.LoginScreen
import org.kodein.di.bindSingleton
import org.kodein.di.compose.localDI
import org.kodein.di.compose.withDI
import org.kodein.di.instance


@Composable
fun MoniotorLogout(navigator: Navigator) {
    val client: SupabaseClient by localDI().instance()

    LaunchedEffect(null) {
        var authenticated: Boolean = false
        client.auth.sessionStatus.collect {
            when (it) {
                is SessionStatus.NotAuthenticated -> {
                    if (authenticated) {
                        navigator.popUntilRoot()
                        navigator.replace(LoginScreen)
                    }
                    authenticated = false
                }
                is SessionStatus.Authenticated -> {
                    authenticated = true
                }
                else -> {}
            }
        }
    }
}

@Composable
fun App() {
    val databaseDriverFactory = rememberDatabaseDriverFactory()
    val scope = rememberCoroutineScope()
    
    withDI({
        bindSingleton { Database(databaseDriverFactory, instance(), scope) }
        bindSingleton { createAppSupabaseClient() }
    }) {
        MaterialTheme {
            Navigator(LoginScreen) {
                MoniotorLogout(it)
                FadeTransition(it)
            }
        }
    }
    
}
