package net.kodein.powerludo.ctrl

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import net.kodein.powerludo.business.Database
import net.kodein.powerludo.db.Boardgame
import org.kodein.di.compose.localDI
import org.kodein.di.instance


object Boardgame {

    data class Model(
        val boardgame: Boardgame,
        val games: List<Games.Game> = emptyList(),
        val deleted: Boolean = false
    )

    sealed interface Intent {
        data object Delete : Intent
    }
    
    @Composable
    fun Components(boardgame: Boardgame) : MviViewComponents<Model, Intent> {
        val database: Database by localDI().instance()
        
        return Mvi(
            firstModel = { Model(boardgame) }
        ) { emit, scope ->

            scope.launch {
                database.boardgame(boardgame.id).collect {
                    if (it != null) {
                        emit { copy(boardgame = it) }
                    } else {
                        emit { copy(deleted = true) }
                    }
                }
            }
            
            scope.launch {
                database.boardgameGames(boardgame.id)
                    .map { games ->
                        games.map { game ->
                            Games.Game(
                                id = game.id,
                                boardgame = boardgame,
                                date = Instant.fromEpochSeconds(game.date).toLocalDateTime(TimeZone.UTC).date,
                                players = database.gamePlayers(game.id).first()
                            )
                        }
                    }
                    .collect { emit { copy(games = it) } }
            }


            OnIntent {
                when (it) {
                    Intent.Delete -> {
                        database.deleteBoardgame(boardgame.id)
                    }
                }
            }
        }
    }
}
