package net.kodein.powerludo.business

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDefaults


@Composable
actual fun rememberUniqueUserId(): String =
    remember {
        val prefs = NSUserDefaults.standardUserDefaults
        prefs.stringForKey("user-id") ?: NSUUID.UUID().UUIDString.also {
            prefs.setObject("user-id", it)
            prefs.synchronize()
        }
    }
