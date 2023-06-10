interface Expr
class BinOp(konst operator : String) : Expr

fun test(e : Expr) {
    if (e is BinOp) {
        when (e.operator) {
            else -> 0
        }
    }
}