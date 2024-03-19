package net.kodein.powerludo

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.FadeTransition
import net.kodein.powerludo.business.Database
import net.kodein.powerludo.business.utils.rememberDatabaseDriverFactory
import net.kodein.powerludo.screen.MainScreen
import org.kodein.di.bindSingleton
import org.kodein.di.compose.withDI


@Composable
fun App() {
    val databaseDriverFactory = rememberDatabaseDriverFactory()
//    val scope = rememberCoroutineScope()
    
    withDI({
        bindSingleton { Database(databaseDriverFactory) }
    }) {
        MaterialTheme {
            Navigator(MainScreen) {
                FadeTransition(it)
            }
        }
    }
    
}
