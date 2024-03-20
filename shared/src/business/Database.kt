package net.kodein.powerludo.business

import com.powersync.DatabaseDriverFactory
import com.powersync.PowerSyncBuilder
import com.powersync.db.schema.Column
import com.powersync.db.schema.IndexedColumn
import com.powersync.db.schema.Table
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.kodein.powerludo.business.model.Boardgame
import net.kodein.powerludo.business.model.Game
import net.kodein.powerludo.business.model.Player
import net.kodein.powerludo.business.utils.Index
import net.kodein.powerludo.business.utils.Schema
import net.kodein.powerludo.business.utils.SupabaseConnector


class Database(
    driverFactory: DatabaseDriverFactory,
    supabaseClient: SupabaseClient,
    scope: CoroutineScope
): CoroutineScope by scope {

    private val database =
        PowerSyncBuilder
            .from(
                factory = driverFactory,
                schema = Schema(
                    Table(
                        name = "boardgame",
                        columns = listOf(
                            Column.text("name"), // text
                            Column.integer("is_coop") // boolean
                        ),
                        indexes = listOf(
                            Index("name", IndexedColumn.ascending("name"))
                        )
                    ),
                    Table(
                        name = "game",
                        columns = listOf(
                            Column.text("boardgame_id"), // uuid
                            Column.integer("date") // date
                        ),
                        indexes = listOf(
                            Index("date", IndexedColumn.descending("date")),
                            Index("boardgame_id", IndexedColumn("boardgame_id"))
                        )
                    ),
                    Table(
                        name = "player",
                        columns = listOf(
                            Column.text("name") // text
                        ),
                        indexes = listOf(
                            Index("name", IndexedColumn.ascending("name"))
                        )
                    ),
                    Table(
                        name = "game_player",
                        columns = listOf(
                            Column.text("game_id"), // uuid
                            Column.text("player_id"), // uuid
                            Column.integer("winner") // boolean
                        ),
                        indexes = listOf(
                            Index("game_id", IndexedColumn("game_id")),
                            Index("player_id", IndexedColumn("player_id"))
                        )
                    )
                )
            )
            //        .scope(scope)
            .build()

    init {
        val connector = SupabaseConnector(
            supabaseClient = supabaseClient,
            powerSyncEndpoint = "https://65f83ef9bae72d5b698f31aa.powersync.journeyapps.com"
        )
        launch {
            database.connect(connector)
        }
    }

    fun boardgames(): Flow<List<Boardgame>> =
        database.watch(
            sql = """
                SELECT *
                FROM boardgame
                ORDER BY name
            """
        ) {
            Boardgame(
                id = it.getString(0)!!,
                name = it.getString(1)!!,
                isCoop = it.getBoolean(2)!!
            )
        }

    fun boardgame(id: String): Flow<Boardgame?> =
        database.watch(
            sql = """
                SELECT *
                FROM boardgame
                WHERE id = ?
            """,
            parameters = listOf(id)
        ) {
            Boardgame(
                id = it.getString(0)!!,
                name = it.getString(1)!!,
                isCoop = it.getBoolean(2)!!
            )
        }.map { it.firstOrNull() }

    suspend fun addBoardgame(name: String, isCoop: Boolean) {
        database.execute(
            sql = "INSERT INTO boardgame (id, name, is_coop), VALUES (uuid(), ?, ?)",
            parameters = listOf(name, isCoop)
        )
    }

    suspend fun deleteBoardgame(id: String) {
        database.writeTransaction {
            database.execute(
                sql = """
                    DELETE
                    FROM game_player
                    WHERE game_id IN (
                        SELECT id
                        FROM game
                        WHERE boardgame_id = ?
                    )
                """,
                parameters = listOf(id)
            )
            database.execute(
                sql = """
                    DELETE
                    FROM game
                    WHERE boardgame_id = ?
                """,
                parameters = listOf(id)
            )
            database.execute(
                sql = """
                    DELETE
                    FROM boardgame
                    WHERE id = ?
                """,
                parameters = listOf(id)
            )
        }
    }

    fun players(): Flow<List<Player>> =
        database.watch(
            sql = """
                SELECT *
                FROM player
                ORDER BY name
            """
        ) {
            Player(
                id = it.getString(0)!!,
                name = it.getString(1)!!
            )
        }

    fun player(id: String): Flow<Player?> =
        database.watch(
            sql = """
                SELECT *
                FROM player
                WHERE id = ?
            """,
            parameters = listOf(id)
        ) {
            Player(
                id = it.getString(0)!!,
                name = it.getString(1)!!
            )
        }.map { it.first() }

    suspend fun addPlayer(name: String) {
        database.execute(
            sql = """
                INSERT
                INTO player (id, name)
                VALUES (uuid(), ?)
            """,
            parameters = listOf(name)
        )
    }

    suspend fun deletePlayer(id: String) {
        database.writeTransaction {
            database.execute(
                sql = """
                    DELETE
                    FROM game_player
                    WHERE player_id = ?
                """,
                parameters = listOf(id)
            )
            database.execute(
                sql = """
                    DELETE
                    FROM player
                    WHERE id = ?
                """,
                parameters = listOf(id)
            )
        }
    }

    fun games(): Flow<List<Game>> =
        database.watch(
            sql = """
                SELECT *
                FROM game
                ORDER BY date DESC
            """
        ) {
            Game(
                id = it.getString(0)!!,
                boardgameId = it.getString(1)!!,
                date = it.getLong(2)!!
            )
        }

    fun boardgameGames(boardgameId: String): Flow<List<Game>> =
        database.watch(
            sql = """
                SELECT *
                FROM game
                WHERE boardgame_id = ?
            """,
            parameters = listOf(boardgameId)
        ) {
            Game(
                id = it.getString(0)!!,
                boardgameId = it.getString(1)!!,
                date = it.getLong(2)!!
            )
        }

    fun playerGames(playerId: String): Flow<List<Pair<Game, Boardgame>>> =
        database.watch(
            sql = """
                SELECT game.*, boardgame.name, boardgame.is_coop
                FROM game_player
                INNER JOIN game ON game.id = game_player.game_id
                INNER JOIN boardgame ON boardgame.id = game.boardgame_id
                WHERE player_id = ?
                ORDER BY game.date
            """,
            parameters = listOf(playerId)
        ) {
            Pair(
                Game(
                    id = it.getString(0)!!,
                    boardgameId = it.getString(1)!!,
                    date = it.getLong(2)!!
                ),
                Boardgame(
                    id = it.getString(1)!!,
                    name = it.getString(3)!!,
                    isCoop = it.getBoolean(4)!!,
                )
            )
        }

    fun gamePlayers(gameId: String): Flow<List<Pair<Player, Boolean>>> =
        database.watch(
            sql = """
                SELECT player.*, winner
                FROM game_player
                INNER JOIN player ON player.id = game_player.player_id
                WHERE game_id = ?
                ORDER BY player.name
            """,
            parameters = listOf(gameId)
        ) {
            Pair(
                Player(
                    id = it.getString(0)!!,
                    name = it.getString(1)!!
                ),
                it.getBoolean(2)!!
            )
        }

    suspend fun addGame(boardgameId: String, date: Long, players: List<Pair<String, Boolean>>) {
        val id = database.get("SELECT uuid()") { it.getString(0)!! }
        database.writeTransaction {
            database.execute(
                sql = """
                    INSERT
                    INTO game (id, boardgame_id, date)
                    VALUES (?, ?, ?)
                """,
                parameters = listOf(id, boardgameId, date)
            )
            players.forEach { (playerId, winner) ->
                database.execute(
                    sql = """
                        INSERT
                        INTO game_player (game_id, player_id, winner)
                        VALUES (?, ?, ?)
                    """,
                    parameters = listOf(id, playerId, winner)
                )
            }
        }
    }

    suspend fun deleteGame(id: String) {
        database.writeTransaction {
            database.execute(
                sql = """
                    DELETE
                    FROM game_player
                    WHERE game_id = ?
                """,
                parameters = listOf(id)
            )
            database.execute(
                sql = """
                    DELETE
                    FROM game
                    WHERE id = ?
                """,
                parameters = listOf(id)
            )
        }
    }

//    fun boardgames(): Flow<List<Boardgame>> = flowOf(emptyList())
//    fun boardgame(id: String): Flow<Boardgame?> = flowOf(null)
//    suspend fun addBoardgame(name: String, isCoop: Boolean) {}
//    suspend fun deleteBoardgame(id: String) {}
//    fun players(): Flow<List<Player>> = flowOf(emptyList())
//    fun player(id: String): Flow<Player?> = flowOf(null)
//    suspend fun addPlayer(name: String) {}
//    suspend fun deletePlayer(id: String) {}
//    fun games(): Flow<List<Game>> = flowOf(emptyList())
//    fun boardgameGames(boardgameId: String): Flow<List<Game>> = flowOf(emptyList())
//    fun playerGames(playerId: String): Flow<List<Pair<Game, Boardgame>>> = flowOf(emptyList())
//    fun gamePlayers(gameId: String): Flow<List<Pair<Player, Boolean>>> = flowOf(emptyList())
//    suspend fun addGame(boardgameId: String, date: Long, players: List<Pair<String, Boolean>>) {}
//    suspend fun deleteGame(id: String) {}
}
