package net.kodein.powerludo.business.utils

import androidx.compose.runtime.Composable
import com.powersync.DatabaseDriverFactory
import com.powersync.db.schema.Index
import com.powersync.db.schema.IndexedColumn
import com.powersync.db.schema.Schema
import com.powersync.db.schema.Table


fun Schema(vararg tables: Table) = Schema(tables.asList())

fun Index(name: String, vararg column: IndexedColumn) = Index(name, column.asList())

@Composable
expect fun rememberDatabaseDriverFactory(): DatabaseDriverFactory
