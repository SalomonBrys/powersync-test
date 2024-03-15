package net.kodein.powerludo

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.FadeTransition
import net.kodein.powerludo.business.Database
import net.kodein.powerludo.business.rememberSqliteDriver
import net.kodein.powerludo.db.LudoDB
import net.kodein.powerludo.screen.MainScreen
import org.kodein.di.bindSingleton
import org.kodein.di.compose.withDI


@Composable
fun App() {
    
    val driver = rememberSqliteDriver(LudoDB.Schema, "ludo.db")

    withDI({
        bindSingleton { Database(driver) }
    }) {
        MaterialTheme {
            Navigator(MainScreen) {
                FadeTransition(it)
            }
        }
    }
    
}
