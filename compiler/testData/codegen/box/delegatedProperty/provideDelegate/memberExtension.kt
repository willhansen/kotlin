// IGNORE_BACKEND: WASM
// WITH_STDLIB

object Host {
    class StringDelegate(konst s: String) {
        operator fun getValue(receiver: String, p: Any) = receiver + s
    }

    operator fun String.provideDelegate(host: Any?, p: Any) = StringDelegate(this)

    konst String.plusK by "K"

    konst ok = "O".plusK
}

fun box(): String = Host.ok
