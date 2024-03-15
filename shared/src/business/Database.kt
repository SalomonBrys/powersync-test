package net.kodein.powerludo.business

import androidx.compose.runtime.Composable
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import net.kodein.powerludo.db.Boardgame
import net.kodein.powerludo.db.Game
import net.kodein.powerludo.db.LudoDB
import net.kodein.powerludo.db.Player


@Composable
expect fun rememberSqliteDriver(schema: SqlSchema<QueryResult.Value<Unit>>, name: String): SqlDriver

class Database(driver: SqlDriver) {
    private val db = LudoDB(driver)
    
    fun boardgames() =
        db.boardgameQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)

    fun boardgame(id: Long) =
        db.boardgameQueries.select(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
    
    suspend fun addBoardgame(name: String, isCoop: Boolean) =
        withContext(Dispatchers.IO) {
            db.boardgameQueries.insert(name, isCoop)
        }
    
    suspend fun deleteBoardgame(id: Long) {
        withContext(Dispatchers.IO) {
            db.transaction {
                db.gamePlayerQueries.deleteForBoardGame(id)
                db.gameQueries.deleteForBoardgame(id)
                db.boardgameQueries.delete(id)
            }
        }
    }

    fun players() =
        db.playerQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)

    fun player(id: Long) =
        db.playerQueries.select(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
    
    suspend fun addPlayer(name: String) =
        withContext(Dispatchers.IO) {
            db.playerQueries.insert(name)
        }

    suspend fun deletePlayer(id: Long) {
        withContext(Dispatchers.IO) {
            db.transaction {
                db.gamePlayerQueries.deleteForPlayer(id)
                db.playerQueries.delete(id)
            }
        }
    }

    fun games() =
        db.gameQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)

    fun boardgameGames(boardgameId: Long) =
        db.gameQueries.selectForBoardgame(boardgameId)
            .asFlow()
            .mapToList(Dispatchers.IO)

    fun playerGames(playerId: Long) =
        db.gamePlayerQueries
            .selectGamesForPlayer(playerId) { id, bgId, date, bgName, bgIsCoop ->
                Game(id, bgId, date) to Boardgame(bgId, bgName, bgIsCoop)
            }
            .asFlow()
            .mapToList(Dispatchers.IO)
    
    fun gamePlayers(gameId: Long) =
        db.gamePlayerQueries
            .selectPlayersForGame(gameId) { id, name, winner ->
                Player(id, name) to winner
            }
            .asFlow()
            .mapToList(Dispatchers.IO)
    
    suspend fun addGame(
        boardgameId: Long,
        date: Long,
        players: List<Pair<Long, Boolean>>
    ) =
        withContext(Dispatchers.IO) {
            db.transaction {
                db.gameQueries.insert(boardgameId, date)
                val gameId = db.gameQueries.lastId().executeAsOne().id!!
                players.forEach { (playerId, winner) ->
                    db.gamePlayerQueries.insert(gameId, playerId, winner)
                }
            }
        }
    
    suspend fun deleteGame(id: Long) {
        withContext(Dispatchers.IO) {
            db.transaction {
                db.gamePlayerQueries.deleteForGame(id)
                db.gameQueries.delete(id)
            }
        }
    }
}
