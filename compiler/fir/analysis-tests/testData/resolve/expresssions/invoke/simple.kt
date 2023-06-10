class Simple {
    operator fun invoke(): String = "invoke"
}

fun test(s: Simple) {
    konst result = s()
}
