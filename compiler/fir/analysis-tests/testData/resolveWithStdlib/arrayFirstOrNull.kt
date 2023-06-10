interface G {
    konst a: Array<out G>
}

fun goo(g: G) {
    konst x = g.a.firstOrNullX()
}

public fun <T> Array<out T>.firstOrNullX(): T? {
    return if (isEmpty()) null else this[0]
}