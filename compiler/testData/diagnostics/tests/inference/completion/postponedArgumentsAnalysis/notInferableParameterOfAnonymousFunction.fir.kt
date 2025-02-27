// IGNORE_LEAKED_INTERNAL_TYPES: KT-54568
// !DIAGNOSTICS: -UNUSED_ANONYMOUS_PARAMETER -UNUSED_VARIABLE

fun <T> select(vararg x: <!CANNOT_INFER_PARAMETER_TYPE, CANNOT_INFER_PARAMETER_TYPE!>T<!>) = x[0]
fun <K> id(x: K) = x

fun main() {
    konst x1 = select<Any?>(id { x, y -> }, { x: Int, y -> })
    konst x2 = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER, NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>select<!>(id { x, y -> }, { x: Int, y -> })

    konst x3 = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER, NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>select<!>(id(fun (x, y) {}), fun (x: Int, y) {})

    konst x4 = select<Any?>((fun (x, y) {}), fun (x: Int, y) {})
    konst x5 = select<Any?>(id(fun (x, y) {}), fun (x: Int, y) {})
    konst x6 = id<Any?>(fun (x) {})

    select<Any?>(fun (x) {}, fun (x) {})
}
