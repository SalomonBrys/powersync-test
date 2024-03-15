package net.kodein.powerludo.business

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.native.NativeSqliteDriver


@Composable
actual fun rememberSqliteDriver(schema: SqlSchema<QueryResult.Value<Unit>>, name: String): SqlDriver {
    return remember { NativeSqliteDriver(schema, name) }
}
