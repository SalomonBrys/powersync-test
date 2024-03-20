package net.kodein.powerludo.ctrl

import androidx.compose.runtime.Composable
import net.kodein.powerludo.business.Database
import org.kodein.di.compose.localDI
import org.kodein.di.instance


object Game {

    data class Model(
        val game: Games.Game,
        val deleted: Boolean = false
    )

    sealed interface Intent {
        data object Delete : Intent
    }
    
    @Composable
    fun Components(game: Games.Game) : MviViewComponents<Model, Intent> {
        val database: Database by localDI().instance()
        
        return Mvi(
            firstModel = { Model(game) }
        ) {
            OnIntent {
                when (it) {
                    Intent.Delete -> {
                        database.deleteGame(game.id)
                    }
                }
            }
        }
    }
}
