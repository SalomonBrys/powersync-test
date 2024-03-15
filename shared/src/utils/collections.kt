package net.kodein.powerludo.utils


fun <T> MutableCollection<T>.toggle(element: T) {
    if (element in this) remove(element)
    else add(element)
}

fun <T> MutableCollection<T>.toggle(element: T, set: Boolean) {
    val present = element in this
    when {
        present && !set -> remove(element)
        !present && set -> add(element)
    }
}
