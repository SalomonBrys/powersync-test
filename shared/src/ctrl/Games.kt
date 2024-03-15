package net.kodein.powerludo.ctrl

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import net.kodein.powerludo.business.Database
import net.kodein.powerludo.db.Boardgame
import net.kodein.powerludo.db.Player
import org.kodein.di.compose.localDI
import org.kodein.di.instance


object Games {

    data class Game(
        val id: Long,
        val date: LocalDate,
        val boardgame: Boardgame,
        val players: List<Pair<Player, Boolean>>
    )
    
    data class Model(
        val boardgames: List<Boardgame> = emptyList(),
        val players: List<Player> = emptyList(),
        val games: List<Game> = emptyList()
    )
    
    sealed interface Intent {
        data class Add(
            val date: LocalDate,
            val boardgameId: Long,
            val playerIds: List<Long>,
            val winnerIds: List<Long>
        ): Intent
    }
    
    @Composable
    fun Components() : MviViewComponents<Model, Intent> {
        val database: Database by localDI().instance()
        
        return Mvi(
            firstModel = ::Model
        ) { emit, scope ->
            
            scope.launch {
                database.boardgames().collect { boardgames ->
                    emit { copy(boardgames = boardgames) }
                }
            }
            
            scope.launch {
                database.players().collect { players ->
                    emit { copy(players = players) }
                }
            }

            scope.launch {
                combine(
                    database.games(),
                    database.boardgames()
                ) { games, boardgames ->
                    games.map { game ->
                        Game(
                            id = game.id,
                            date = Instant.fromEpochSeconds(game.date).toLocalDateTime(TimeZone.UTC).date,
                            boardgame = boardgames.first { it.id == game.boardgameId },
                            players = database.gamePlayers(game.id).first()
                        )
                    }
                }.collect {
                    emit { copy(games = it) }
                }
            }
            
            OnIntent { intent ->
                when (intent) {
                    is Intent.Add -> {
                        database.addGame(
                            boardgameId = intent.boardgameId,
                            date = intent.date.atStartOfDayIn(TimeZone.UTC).epochSeconds,
                            players = intent.playerIds.map { pid ->
                                pid to (pid in intent.winnerIds)
                            }
                        )
                    }
                }
            }
        }
    }
}
