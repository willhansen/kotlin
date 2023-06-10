// TARGET_BACKEND: JVM
// WITH_STDLIB

class C<K> @JvmOverloads constructor(konst s: String = "OK")

fun box() = C<Unit>().s
