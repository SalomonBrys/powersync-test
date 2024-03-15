package net.kodein.powerludo.ctrl

import androidx.compose.runtime.Composable
import kotlinx.coroutines.launch
import net.kodein.powerludo.business.Database
import net.kodein.powerludo.db.Player
import org.kodein.di.compose.localDI
import org.kodein.di.instance


object Players {
    
    data class Model(
        val players: List<Player> = emptyList()
    )
    
    sealed interface Intent {
        data class Add(val name: String): Intent
    }
    
    @Composable
    fun Components() : MviViewComponents<Model, Intent> {
        val database: Database by localDI().instance()
        
        return Mvi(
            firstModel = ::Model
        ) { emit, scope ->
            
            scope.launch {
                database.players().collect { players ->
                    emit { copy(players = players) }
                }
            }
            
            
            OnIntent {
                when (it) {
                    is Intent.Add -> {
                        database.addPlayer(it.name)
                    }
                }
            }
        }
    }
}
