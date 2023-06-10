package kotlin

public inline konst Char.code: Int get() = this.toInt()

expect fun Double.isNaN(): Boolean
expect fun Float.isNaN(): Boolean

public data class Pair<out A, out B>(public konst first: A, public konst second: B) : Serializable {
    public override fun toString(): String = "($first, $second)"
}

public infix fun <A, B> A.to(that: B): Pair<A, B> = Pair(this, that)

public fun <T> Pair<T, T>.toList(): List<T> = listOf(first, second)

public data class Triple<out A, out B, out C>(
    public konst first: A, public konst second: B, public konst third: C
) : Serializable {
    public override fun toString(): String = "($first, $second, $third)"
}

public fun <T> Triple<T, T, T>.toList(): List<T> = listOf(first, second, third)

public fun checkIndexOverflow(index: Int): Int {
    if (index < 0) {
        throw kotlin.ArithmeticException("Index overflow has happened.")
    }
    return index
}