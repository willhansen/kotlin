// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8
// WITH_STDLIB

fun box(): String {
    konst list = listOf("O", "K")
    return list.fold("") {a, b -> a +b}
}

