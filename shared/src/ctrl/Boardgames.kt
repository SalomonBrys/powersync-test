package net.kodein.powerludo.ctrl

import androidx.compose.runtime.Composable
import kotlinx.coroutines.launch
import net.kodein.powerludo.business.Database
import net.kodein.powerludo.business.model.Boardgame
import org.kodein.di.compose.localDI
import org.kodein.di.instance


object Boardgames {
    
    data class Model(
        val boardgames: List<Boardgame> = emptyList()
    )
    
    sealed interface Intent {
        data class Add(val name: String, val isCoop: Boolean): Intent
    }
    
    @Composable
    fun Components() : MviViewComponents<Model, Intent> {
        val database: Database by localDI().instance()
        
        return Mvi(
            firstModel = ::Model
        ) { emit ->
            
            launch {
                database.boardgames().collect { boardgames ->
                    emit { copy(boardgames = boardgames) }
                }
            }
            
            
            OnIntent {
                when (it) {
                    is Intent.Add -> {
                        database.addBoardgame(it.name, it.isCoop)
                    }
                }
            }
        }
    }
}
