package net.kodein.powerludo.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import net.kodein.powerludo.db.Boardgame
import net.kodein.powerludo.utils.europeanFormat
import net.kodein.powerludo.ctrl.Boardgame as BoardgameCtrl


class BoardgameScreen(val initial: Boardgame) : Screen {

    override val key = uniqueScreenKey
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val (model, emit) = BoardgameCtrl.Components(initial)
        val navigator = LocalNavigator.currentOrThrow

        remember(model.deleted) {
            if (model.deleted) navigator.pop()
        }
        
        var deleteDialogShown by remember { mutableStateOf(false) }
        
        if (deleteDialogShown) {
            AlertDialog(
                onDismissRequest = { deleteDialogShown = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            emit(BoardgameCtrl.Intent.Delete)
                            deleteDialogShown = false
                        }
                    ) { Text("DELETE") }
                },
                dismissButton = {
                    TextButton(
                        onClick = { deleteDialogShown = false }
                    ) { Text("KEEP") }
                },
                title = { Text("Confirm deletion") },
                text = { Text("Delete ${model.boardgame.name}?") }
            )
        }
        
        Scaffold(
            topBar = {
                LargeTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BoardgameIcon(model.boardgame, Modifier.height(48.dp))
                            Text(
                                text = model.boardgame.name,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { navigator.pop() }
                        ) { Icon(Icons.Filled.ArrowBack, "Back") }
                    },
                    actions = {
                        IconButton(
                            onClick = { deleteDialogShown = true }
                        ) { Icon(Icons.Filled.Delete, "Delete") }
                    }
                )
            }
        ) {
            GamesList(
                games = model.games,
                showBoardgame = false,
                modifier = Modifier.padding(it).fillMaxSize()
            )
        }
    }
}