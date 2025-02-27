interface Source<out T> {
    fun nextT(): T
}

fun demo(strs: Source<String>) {
    konst objects: Source<Any> = strs
}

interface Comparable<in T> {
    operator fun compareTo(other: T): Int
}

fun demo(x: Comparable<Number>) {
    x.compareTo(1.0)
    konst y: Comparable<Double> = x
}