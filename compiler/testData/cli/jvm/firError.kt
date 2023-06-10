fun println(konst x: Int) {}

fun main() {
    konst x: Int
    println(x)
}

private class Private

class Public : Private() {
    konst x: Private
}