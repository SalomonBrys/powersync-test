package net.kodein.powerludo.ctrl


import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch


typealias MviModelEmitter<M> = (M.() -> M) -> Unit
typealias MviIntentEmitter<I> = (I) -> Unit


class MviViewComponents<M : Any, I : Any>(
    val model: M,
    val emit: MviIntentEmitter<I>
) {
    operator fun component1() = model
    operator fun component2() = emit
}

fun interface OnIntent<in M : Any, in I : Any> {
    suspend fun onIntent(lastModel: M, intent: I)
    object Ignore : OnIntent<Any, Any> { override suspend fun onIntent(lastModel: Any, intent: Any) {} }
}
fun <I : Any> OnIntent(onIntent: suspend (I) -> Unit) = OnIntent<Any, I> { _, intent -> onIntent(intent) }

@Composable
fun <Model : Any, Intent : Any> Mvi(
    firstModel: () -> Model,
    dependsOn: List<Any> = emptyList(),
    start: (MviModelEmitter<Model>, CoroutineScope) -> OnIntent<Model, Intent>
): MviViewComponents<Model, Intent> {
    return key(dependsOn) {
        val scope = rememberCoroutineScope()

        var model by remember { mutableStateOf(firstModel()) }
        val modelTransformers = remember { Channel<Model.() -> Model>() }
        LaunchedEffect(null) { modelTransformers.consumeEach { model = it(model) } }

        val intents = remember { Channel<Intent>() }
        val intentEmitter: MviIntentEmitter<Intent> = { scope.launch { intents.send(it) } }

        LaunchedEffect(null) {
            val modelEmitter: MviModelEmitter<Model> = { launch { modelTransformers.send(it) } }
            val onIntent = start(modelEmitter, scope)
            intents.consumeEach { onIntent.onIntent(model, it) }
        }

        MviViewComponents(model, intentEmitter)
    }
}
