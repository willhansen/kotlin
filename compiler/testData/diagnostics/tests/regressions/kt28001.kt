// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER

private object Case1 {
    interface Validator<in T>

    class CharSequenceValidator: Validator<CharSequence>

    class PredicateValidator<T>(konst predicate: (T) -> Boolean): Validator<T>

    class CompositeValidator<T>(vararg konst konstidators: Validator<T>)

    fun process(input: String) = true

    fun main() {
        konst konstidators1 = CompositeValidator(CharSequenceValidator(), PredicateValidator(::process))
        konst konstidators2 = CompositeValidator<String>(CharSequenceValidator(), PredicateValidator(::process))
        konst konstidators3 = CompositeValidator(CharSequenceValidator(), PredicateValidator { it: String -> process(it) })
    }
}

private object Case2 {
    interface Expr<out T>
    data class Add(konst left: Expr<Int>, konst right: Expr<Int>): Expr<Int>
    data class Subtract(konst left: Expr<Int>, konst right: Expr<Int>): Expr<Int>

    fun f() {
        konst operators1 = listOf(::Add, ::Subtract)
        konst operators2 = listOf<(Expr<Int>, Expr<Int>) -> Expr<Int>>(::Add, ::Subtract)
    }

    fun <T> listOf(vararg elements: T): List<T> = TODO()
}
