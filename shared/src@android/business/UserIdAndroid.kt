package net.kodein.powerludo.business

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.UUID


@Composable
actual fun rememberUniqueUserId(): String {
    val context = LocalContext.current
    
    return remember {
        val prefs = context.getSharedPreferences("PowerLudo", Context.MODE_PRIVATE)
        prefs.getString("user-id", null) ?: UUID.randomUUID().toString().also {
            prefs.edit().putString("user-id", it).apply()
        }
    }
}
