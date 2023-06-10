internal class Method<out T : String?>private constructor(konst name: String, konst signature: T) {
    companion object {
        operator fun invoke(name: String): Method<String?> = TODO()
    }
}

fun foo() {
    Method("asd")
}
