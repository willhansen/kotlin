package bar

@Suppress("REDECLARATION")
konst foo: Int = 6

operator fun Int.invoke() = this

fun main() {
    println(foo())
}