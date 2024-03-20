package net.kodein.powerludo.business.utils

import androidx.compose.runtime.Composable
import com.powersync.DatabaseDriverFactory
import com.powersync.PowerSyncDatabase
import com.powersync.connectors.PowerSyncBackendConnector
import com.powersync.connectors.PowerSyncCredentials
import com.powersync.db.crud.CrudEntry
import com.powersync.db.crud.UpdateType
import com.powersync.db.schema.Index
import com.powersync.db.schema.IndexedColumn
import com.powersync.db.schema.Schema
import com.powersync.db.schema.Table
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from


fun Schema(vararg tables: Table) = Schema(tables.asList())

fun Index(name: String, vararg column: IndexedColumn) = Index(name, column.asList())

@Composable
expect fun rememberDatabaseDriverFactory(): DatabaseDriverFactory


class SupabaseConnector(
    val supabaseClient: SupabaseClient,
    val powerSyncEndpoint: String
) : PowerSyncBackendConnector() {

    constructor(
        supabaseUrl: String,
        supabaseKey: String,
        powerSyncEndpoint: String
    ) : this(
        supabaseClient = createSupabaseClient(supabaseUrl, supabaseKey) {
            install(Auth)
            install(Postgrest)
        },
        powerSyncEndpoint = powerSyncEndpoint
    )

    init {
        require(supabaseClient.pluginManager.getPluginOrNull(Auth) != null) { "The Auth plugin must be installed on the Supabase client" }
        require(supabaseClient.pluginManager.getPluginOrNull(Postgrest) != null) { "The Postgrest plugin must be installed on the Supabase client" }
    }

    /**
     * Get credentials for PowerSync.
     */
    override suspend fun fetchCredentials(): PowerSyncCredentials {
        check(supabaseClient.auth.sessionStatus.value is SessionStatus.Authenticated) { "Not Authenticated" }

        // Use Supabase token for PowerSync
        val session = supabaseClient.auth.currentSessionOrNull() ?: error("Could not fetch Supabase credentials");

        check(session.user != null) { "No user data" }

        // userId and expiresAt are for debugging purposes only
        return PowerSyncCredentials(
            endpoint = powerSyncEndpoint,
            token = session.accessToken, // Use the access token to authenticate against PowerSync
            expiresAt = session.expiresAt,
            userId = session.user!!.id
        )
    }

    /**
     * Upload local changes to the app backend (in this case Supabase).
     *
     * This function is called whenever there is data to upload, whether the device is online or offline.
     * If this call throws an error, it is retried periodically.
     */
    override suspend fun uploadData(database: PowerSyncDatabase) {

        val transaction = database.getNextCrudTransaction() ?: return;

        var lastEntry: CrudEntry? = null;
        try {

            for (entry in transaction.crud) {
                lastEntry = entry;

                val table = supabaseClient.from(entry.table)
                when (entry.op) {
                    UpdateType.PUT -> {
                        val data = entry.opData?.toMutableMap() ?: mutableMapOf()
                        data["id"] = entry.id
                        table.upsert(data)
                    }

                    UpdateType.PATCH -> {
                        table.update(entry.opData!!) {
                            filter {
                                eq("id", entry.id)
                            }
                        }
                    }

                    UpdateType.DELETE -> {
                        table.delete {
                            filter {
                                eq("id", entry.id)
                            }
                        }
                    }
                }
            }

            transaction.complete(null);

        } catch (e: Exception) {
            println("Data upload error - retrying last entry: ${lastEntry!!}, $e")
            throw e
        }
    }
}