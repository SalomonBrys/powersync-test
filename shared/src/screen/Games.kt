package net.kodein.powerludo.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import net.kodein.powerludo.business.model.Boardgame
import net.kodein.powerludo.business.model.Player
import net.kodein.powerludo.ctrl.Games
import net.kodein.powerludo.ctrl.MviIntentEmitter
import net.kodein.powerludo.utils.europeanFormat
import net.kodein.powerludo.utils.toggle


@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SelectDate(
    selected: LocalDate,
    onSelect: (LocalDate) -> Unit
) {
    var dialogShown by remember { mutableStateOf(false) }

    TextButton(
        onClick = { dialogShown = true }
    ) {
        Text(europeanFormat.format(selected))
        Icon(Icons.Filled.ArrowDropDown, "Select")
    }

    if (dialogShown) {
        val datePickerState = rememberDatePickerState(selected.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds())
        DatePickerDialog(
            onDismissRequest = { dialogShown = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { onSelect(Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.currentSystemDefault()).date) }
                        dialogShown = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        dialogShown = false
                    }
                ) { Text("CANCEL") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SelectBoardgame(
    boardgames: List<Boardgame>,
    selected: Boardgame?,
    onSelect: (Boardgame) -> Unit
) {
    var sheetShown by remember { mutableStateOf(false) }

    TextButton(
        onClick = { sheetShown = true }
    ) {
        if (selected != null) {
            BoardgameIcon(selected, Modifier.height(24.dp))
        }
        Text(
            text = selected?.name ?: "SELECT BOARD GAME",
            style = MaterialTheme.typography.bodyLarge
        )
        Icon(Icons.Filled.ArrowDropDown, "Select")
    }

    if (sheetShown) {
        val scope = rememberCoroutineScope()
        val sheetState: SheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = {
                sheetShown = false
            },
            sheetState = sheetState,
            ) {
            BoardgameList(
                list = boardgames,
                onClick = {
                    onSelect(it)
                    scope.launch {
                        sheetState.hide()
                        sheetShown = false
                    }
                },
                size = ListSize.Small
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SelectPlayers(
    players: List<Player>,
    selected: List<String>,
    onSelect: (String) -> Unit,
    winners: List<String>,
    onSetWinner: (String, Boolean) -> Unit
) {
    var sheetShown by remember { mutableStateOf(false) }

    TextButton(
        onClick = { sheetShown = true }
    ) {
        Text("SELECT PLAYERS")
        Icon(Icons.Filled.ArrowDropDown, "Select")
    }

    if (selected.isNotEmpty()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(Modifier.weight(1f))
            Text("Win?", textAlign = TextAlign.Center, modifier = Modifier.width(48.dp))
        }
        selected
            .map { id -> players.first { it.id == id } }
            .sortedBy { it.name }
            .forEach { player ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(player.name, Modifier.weight(1f).padding(start = 16.dp))
                    Box(Modifier.width(48.dp)) {
                        Checkbox(
                            checked = player.id in winners,
                            onCheckedChange = { onSetWinner(player.id, it) }
                        )
                    }
                }
            }
    }
    
    if (sheetShown) {
        ModalBottomSheet(
            onDismissRequest = {
                sheetShown = false
            },
            ) {
            PlayersList(
                list = players,
                onClick = { onSelect(it.id) },
                size = ListSize.Small,
                selectedIds = selected,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun AddGameDialog(
    onDismissRequest: () -> Unit,
    model: Games.Model,
    emit: MviIntentEmitter<Games.Intent>
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        var date: LocalDate by remember {
            mutableStateOf(
                Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date
            )
        }
        var boardgame: Boardgame? by remember { mutableStateOf(null) }
        val players: MutableList<String> = remember { mutableStateListOf() }
        val winners: MutableList<String> = remember { mutableStateListOf() }

        Surface(modifier = Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize()) {
                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Filled.Close, "Close")
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text("Add Game", style = MaterialTheme.typography.titleLarge)
                    
                    SelectDate(
                        selected = date,
                        onSelect = { date = it }
                    )

                    SelectBoardgame(
                        boardgames = model.boardgames,
                        selected = boardgame,
                        onSelect = {
                            boardgame = it
                            winners.clear()
                        }
                    )
                    
                    if (boardgame != null) {
                        SelectPlayers(
                            players = model.players,
                            selected = players,
                            onSelect = { players.toggle(it) },
                            winners = winners,
                            onSetWinner = { pId, win ->
                                if (boardgame!!.isCoop) {
                                    winners.clear()
                                    if (win) winners.addAll(players)
                                } else {
                                    winners.toggle(pId, win)
                                }
                            }
                        )
                    }
                    
                    if (boardgame != null && players.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {
                                emit(Games.Intent.Add(
                                    date = date,
                                    boardgameId = boardgame!!.id,
                                    playerIds = players,
                                    winnerIds = winners
                                ))
                                onDismissRequest()
                            }
                        ) {
                            Text("ADD GAME")
                        }
                    }
                }
            }
        }
    }
    
}

@Composable
fun GamePlayers(players: List<Pair<Player, Boolean>>) {
    Text(
        text = buildAnnotatedString {
            append("With ")
            players.forEachIndexed { index, (player, winner) ->
                when {
                    index == players.lastIndex -> append(", and ")
                    index != 0 -> append(", ")
                }
                if (winner) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(player.name)
                        append("★")
                    }
                } else {
                    append(player.name)
                }
            }
            append(".")
        },
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
fun GamesList(
    games: List<Games.Game>,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    showBoardgame: Boolean = true
) {
    var gameDialogGame: Games.Game? by remember { mutableStateOf(null) }
    if (gameDialogGame != null) {
        GameDialog(
            onDismissRequest = { gameDialogGame = null },
            game = gameDialogGame!!
        )
    }

    LazyColumn(
        modifier = modifier,
        state = state
    ) {
        items(games) { game ->
            Column(
                modifier = Modifier
                    .fillParentMaxWidth()
                    .clickable { gameDialogGame = game }
                    .padding(16.dp)
            ) {
                Text(text = europeanFormat.format(game.date), style = MaterialTheme.typography.bodySmall)
                if (showBoardgame) {
                    Text(text = game.boardgame.name, style = MaterialTheme.typography.titleMedium)
                }
                GamePlayers(game.players)
            }
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
        }
    }
}

@Composable
fun Games(model: Games.Model, emit: MviIntentEmitter<Games.Intent>) {
    var addDialogShown by remember { mutableStateOf(false) }
    if (addDialogShown) {
        AddGameDialog(
            onDismissRequest = { addDialogShown = false },
            model = model,
            emit = emit
        )
    }
    
    val lazyListState = rememberLazyListState()

    Scaffold(
        floatingActionButton = {
            if (model.boardgames.isNotEmpty() && model.players.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { addDialogShown = true },
                    text = { Text("ADD GAME") },
                    icon = { Icon(Icons.Filled.Add, "Add game") },
                    expanded = !lazyListState.canScrollBackward && !lazyListState.isScrollInProgress
                )
            }
        }
    ) { padding ->
        GamesList(
            games = model.games,
            state = lazyListState,
            modifier = Modifier.padding(padding).fillMaxSize()
        )
    }
    
}