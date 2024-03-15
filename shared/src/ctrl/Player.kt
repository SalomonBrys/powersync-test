package net.kodein.powerludo.ctrl

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import net.kodein.powerludo.business.Database
import net.kodein.powerludo.db.Player
import org.kodein.di.compose.localDI
import org.kodein.di.instance


object Player {

    data class Model(
        val player: Player,
        val games: List<Games.Game> = emptyList(),
        val deleted: Boolean = false
    )

    sealed interface Intent {
        data object Delete : Intent
    }

    @Composable
    fun Components(player: Player) : MviViewComponents<Model, Intent> {
        val database: Database by localDI().instance()
        
        return Mvi(
            firstModel = { Model(player) }
        ) { emit, scope ->

            scope.launch {
                database.player(player.id).collect {
                    if (it != null) {
                        emit { copy(player = it) }
                    } else {
                        emit { copy(deleted = true) }
                    }
                }
            }
            
            scope.launch {
                database.playerGames(player.id)
                    .map { games ->
                        games.map { (game, boardgame) ->
                            Games.Game(
                                id = game.id,
                                date = Instant.fromEpochSeconds(game.date).toLocalDateTime(TimeZone.UTC).date,
                                boardgame = boardgame,
                                players = database.gamePlayers(game.id).first()
                            )
                        }
                    }
                    .collect { emit { copy(games = it) } }
            }


            OnIntent {
                when (it) {
                    Intent.Delete -> {
                        database.deletePlayer(player.id)
                    }
                }
            }
        }
    }
}
