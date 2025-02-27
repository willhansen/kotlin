interface Source<out T> {
    fun nextT(): T
}

fun demo(strs: Source<String>) {
//      Source<Any>            demo.strs: Source<String>
//      │                      │
    konst objects: Source<Any> = strs
}

interface Comparable<in T> {
    operator fun compareTo(other: T): Int
}

fun demo(x: Comparable<Number>) {
//  demo.x: Comparable<Number>
//  │ fun (Comparable<T>).compareTo(T): Int
//  │ │         Double
//  │ │         │
    x.compareTo(1.0)
//      Comparable<Double>      demo.x: Comparable<Number>
//      │                       │
    konst y: Comparable<Double> = x
}
