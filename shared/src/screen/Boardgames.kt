package net.kodein.powerludo.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import net.kodein.powerludo.ctrl.Boardgames
import net.kodein.powerludo.ctrl.MviIntentEmitter
import net.kodein.powerludo.db.Boardgame


@Composable
fun BoardgameIcon(boardgame: Boardgame, modifier: Modifier = Modifier) {
    if (boardgame.isCoop) Icon(Icons.Filled.Handshake, "Competitive", modifier = modifier)
    else Icon(Icons.Filled.MilitaryTech, "Cooperative", modifier = modifier)
}

@Composable
fun BoardgameList(
    list: List<Boardgame>,
    onClick: (Boardgame) -> Unit,
    state: LazyListState = rememberLazyListState(),
    size: ListSize = ListSize.Big
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = state
    ) {
        items(
            items = list
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillParentMaxWidth()
                    .clickable { onClick(it) }
                    .padding(size.padding)
            ) {
                BoardgameIcon(it, Modifier.height(size.iconHeight))
                Text(
                    text = it.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                )
            }
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
        }
    }
    
}

@Composable
private fun AddBoardgameDialog(
    onDismissRequest: () -> Unit,
    addBoardgame: (String, Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var isCoop by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    addBoardgame(name, isCoop)
                    onDismissRequest()
                },
                enabled = name.isNotEmpty()
            ) { Text("ADD") }
                        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) { Text("CANCEL") }
        },
        title = { Text("Add board game") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { isCoop = !isCoop }
                        .padding(8.dp)
                ) {
                    Checkbox(
                        checked = isCoop,
                        onCheckedChange = null
                    )
                    Text(" Cooperative")
                }
            }
        },
        modifier = Modifier.padding(8.dp)
    )
}

@Composable
fun Boardgames(model: Boardgames.Model, emit: MviIntentEmitter<Boardgames.Intent>) {
    val lazyListState = rememberLazyListState()
    
    var addDialogShown by remember { mutableStateOf(false) }
    if (addDialogShown) {
        AddBoardgameDialog(
            onDismissRequest = { addDialogShown = false },
            addBoardgame = { name, isCoop -> emit(Boardgames.Intent.Add(name, isCoop)) }
        )
    }
    
    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { addDialogShown = true },
                text = { Text("ADD BOARD GAME") },
                icon = { Icon(Icons.Filled.Add, "Add board game") },
                expanded = !lazyListState.canScrollBackward && !lazyListState.isScrollInProgress
            )
        }
    ) {
        val navigator = LocalNavigator.currentOrThrow
        BoardgameList(
            list = model.boardgames,
            onClick = { navigator.push(BoardgameScreen(it)) },
            state = lazyListState
        )
    }
}
