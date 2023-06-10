// FIR_IDENTICAL
// SKIP_KLIB_TEST
sealed class Expr {
    class Const(konst number: Double) : Expr()
    class Sum(konst e1: Expr, konst e2: Expr) : Expr()
    object NotANumber : Expr()
}
