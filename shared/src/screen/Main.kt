package net.kodein.powerludo.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import net.kodein.powerludo.ctrl.Boardgames
import net.kodein.powerludo.ctrl.Games
import net.kodein.powerludo.ctrl.Main
import net.kodein.powerludo.ctrl.Players


object MainScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val (_, emit) = Main.Components()

        Column(Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("PowerLudo") },
                actions = {
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(8.dp),
                        tooltip = { PlainTooltip { Text("Log out") } },
                        state = rememberTooltipState()
                    ) {
                        IconButton(
                            onClick = { emit(Main.Intent.Logout) },
                        ) {
                            Icon(Icons.Filled.Logout, contentDescription = "Log out")
                        }
                    }
                }
            )
            var selectedTabIndex by rememberSaveable { mutableStateOf(0) }
            TabRow( // TODO: Should be PrimaryTabRow
                selectedTabIndex = selectedTabIndex
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("LIBRARY") },
                    icon = { Icon(Icons.Filled.LibraryBooks, "Library") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("PLAYERS") },
                    icon = { Icon(Icons.Filled.Groups, "Players") }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = { Text("GAMES") },
                    icon = { Icon(Icons.Filled.Casino, "Games") }
                )
            }
            val (boardgamesModel, boardgamesEmit) = Boardgames.Components()
            val (gamesModel, gamesEmit) = Games.Components()
            val (playersModel, playersEmit) = Players.Components()
            when (selectedTabIndex) {
                0 -> Boardgames(boardgamesModel, boardgamesEmit)
                1 -> Players(playersModel, playersEmit)
                2 -> Games(gamesModel, gamesEmit)
                else -> {}
            }
        }
    }

}
