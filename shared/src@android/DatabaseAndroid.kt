package net.kodein.powerludo.business

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.android.AndroidSqliteDriver


@Composable
actual fun rememberSqliteDriver(schema: SqlSchema<QueryResult.Value<Unit>>, name: String): SqlDriver {
    val context = LocalContext.current
    return remember { AndroidSqliteDriver(schema, context, name) }
}
