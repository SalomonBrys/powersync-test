package net.kodein.powerludo.ctrl

import androidx.compose.runtime.Composable
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.exceptions.BadRequestRestException
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.launch
import org.kodein.di.compose.localDI
import org.kodein.di.instance


object Login {

    sealed interface State {
        data object DisplayForm : State
        data object Authenticated : State
        data object Loading : State
        data class Error(val error: String) : State
    }
    
    data class Model(
        val state: State = State.Loading,
    )
    
    sealed interface Intent {
        data class Login(val email: String, val password: String) : Intent
    }
    
    @Composable
    fun Components() : MviViewComponents<Model, Intent> {
        
        val client: SupabaseClient by localDI().instance()
        
        return Mvi(
            firstModel = ::Model
        ) { emit ->
            launch {
                client.auth.sessionStatus.collect {
                    when (it) {
                        is SessionStatus.Authenticated -> emit { copy(state = State.Authenticated) }
                        is SessionStatus.LoadingFromStorage -> emit { copy(state = State.Loading) }
                        is SessionStatus.NetworkError -> emit { copy(state = State.Error("Network error")) }
                        is SessionStatus.NotAuthenticated -> emit { copy(state = State.DisplayForm) }
                    }
                }
            }
            
            OnIntent {
                when (it) {
                    is Intent.Login -> {
                        emit { copy(state = State.Loading) }
                        try {
                            client.auth.signInWith(Email) {
                                email = it.email
                                password = it.password
                            }
                        } catch (e: BadRequestRestException) {
                            emit { copy(state = State.DisplayForm) }
                        } catch (e: Throwable) {
                            emit { copy(state = State.Error(e.message ?: e::class.simpleName ?: "Error")) }
                        }
                    }
                }
            }
        }
    }
    
}