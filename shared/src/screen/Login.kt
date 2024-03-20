package net.kodein.powerludo.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.compose.auth.ui.AuthForm
import io.github.jan.supabase.compose.auth.ui.LocalAuthState
import io.github.jan.supabase.compose.auth.ui.email.OutlinedEmailField
import io.github.jan.supabase.compose.auth.ui.password.OutlinedPasswordField
import net.kodein.powerludo.ctrl.Login


object LoginScreen : Screen {

    @OptIn(SupabaseExperimental::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val (model, emit) = Login.Components()
        
        remember(model.state) {
            if (model.state is Login.State.Authenticated) {
                navigator.replace(MainScreen)
            }
        }
        
        var email by remember { mutableStateOf("test@test.com") }
        var password by remember { mutableStateOf("test") }
        
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            when (model.state) {
                is Login.State.Authenticated, is Login.State.Loading -> {
                    CircularProgressIndicator(Modifier.size(128.dp))
                }
                is Login.State.Error -> {
                    Text(
                        text = model.state.error,
                        color = Color.Red,
                        fontSize = 24.sp
                    )
                }
                is Login.State.DisplayForm -> {
                    AuthForm {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            OutlinedEmailField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("E-Mail") },
                                mandatory = email.isNotBlank()
                            )
                            OutlinedPasswordField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password") }
                            )
                            Button(
                                onClick = {
                                    emit(Login.Intent.Login(email, password))
                                },
                                enabled = LocalAuthState.current.validForm
                            ) {
                                Text("Login")
                            }
                        }                    
                    }
                }
            }
        }            
    }
}
