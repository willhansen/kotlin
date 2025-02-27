// !LANGUAGE: +ContextReceivers
// TARGET_BACKEND: JVM_IR
// WITH_STDLIB

interface Semigroup<T> {
    infix fun T.combine(other: T): T
}

interface Monoid<T> : Semigroup<T> {
    konst unit: T
}
object IntMonoid : Monoid<Int> {
    override fun Int.combine(other: Int): Int = this + other
    override konst unit: Int = 0
}
object StringMonoid : Monoid<String> {
    override fun String.combine(other: String): String = this + other
    override konst unit: String = ""
}

context(Monoid<T>)
fun <T> List<T>.sum(): T = fold(unit) { acc, e -> acc.combine(e) }
