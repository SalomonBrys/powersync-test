package net.kodein.powerludo.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import net.kodein.powerludo.ctrl.Game
import net.kodein.powerludo.ctrl.Games


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDialog(
    onDismissRequest: () -> Unit,
    game: Games.Game,
) {
    val navigator = LocalNavigator.currentOrThrow
    val (model, emit) = Game.Components(game)
    
    remember(model.deleted) {
        if (model.deleted) {
            onDismissRequest()
        }
    }
    
    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Surface(
            shape = MaterialTheme.shapes.large
        ) {
            Column {
                MediumTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable {
                                    navigator.push(BoardgameScreen(model.game.boardgame))
                                    onDismissRequest()
                                }
                        ) {
                            BoardgameIcon(model.game.boardgame, Modifier.height(24.dp))
                            Text(model.game.boardgame.name)
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onDismissRequest
                        ) { Icon(Icons.Filled.Close, "Close") }
                    },
                    actions = {
                        IconButton(
                            onClick = { emit(Game.Intent.Delete) }
                        ) { Icon(Icons.Filled.Delete, "Delete") }
                    }
                )
                PlayersList(
                    list = model.game.players.map { it.first },
                    onClick = {
                        navigator.push(PlayerScreen(it))
                        onDismissRequest()
                    }
                )
            }
        }
    }
}
