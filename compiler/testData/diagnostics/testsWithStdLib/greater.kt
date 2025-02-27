// FIR_IDENTICAL
class Expression<T>(konst x: T)

class GreaterOp(konst expr1: Expression<*>, konst expr2: Expression<*>)

fun <T : Comparable<T>, S : T?> Expression<in S>.greater(other: T): GreaterOp =
    GreaterOp(this, Expression(other))

fun foo(countExpr: Expression<Long>) {
    countExpr.greater(0)
    countExpr.<!INFERRED_TYPE_VARIABLE_INTO_EMPTY_INTERSECTION_WARNING!>greater<!>("0")
    countExpr.greater<String, Nothing>("0")
}
