package net.kodein.powerludo.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import net.kodein.powerludo.ctrl.MviIntentEmitter
import net.kodein.powerludo.ctrl.Players
import net.kodein.powerludo.db.Player


@Composable
fun PlayersList(
    list: List<Player>,
    onClick: (Player) -> Unit,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    size: ListSize = ListSize.Big,
    selectedIds: List<Long> = emptyList()
) {
    LazyColumn(
        modifier = modifier,
        state = state
    ) {
        items(
            items = list
        ) {
            val selected = it.id in selectedIds
            val background by animateColorAsState(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillParentMaxWidth()
                    .background(background)
                    .clickable { onClick(it) }
                    .padding(size.padding)
            ) {
                if (selected) {
                    Icon(Icons.Filled.Check, "Competitive", modifier = Modifier.height(size.iconHeight))
                } else {
                    Icon(Icons.Filled.Person, "Competitive", modifier = Modifier.height(size.iconHeight))
                }
                Text(
                    text = it.name,
                    style = MaterialTheme.typography.titleMedium,
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
private fun AddPlayerDialog(
    onDismissRequest: () -> Unit,
    addPlayer: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    addPlayer(name)
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
        title = { Text("Add Player") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        modifier = Modifier.padding(8.dp)
    )
    
}

@Composable
fun Players(model: Players.Model, emit: MviIntentEmitter<Players.Intent>) {
    val lazyListState = rememberLazyListState()
    val navigator = LocalNavigator.currentOrThrow
    
    var addDialogShown by remember { mutableStateOf(false) }
    if (addDialogShown) {
        AddPlayerDialog(
            onDismissRequest = { addDialogShown = false },
            addPlayer = { emit(Players.Intent.Add(it)) }
        )
    }
    
    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { addDialogShown = true },
                text = { Text("ADD PLAYER") },
                icon = { Icon(Icons.Filled.Add, "Add boardgame") },
                expanded = !lazyListState.canScrollBackward && !lazyListState.isScrollInProgress
            )
        }
    ) {
        PlayersList(
            list = model.players,
            onClick = { navigator.push(PlayerScreen(it)) },
            state = lazyListState,
            modifier = Modifier.padding(it).fillMaxSize()
        )
    }
}
