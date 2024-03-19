package net.kodein.powerludo.business.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.powersync.DatabaseDriverFactory

@Composable
actual fun rememberDatabaseDriverFactory(): DatabaseDriverFactory =
    remember {
        DatabaseDriverFactory()
    }
