// CHECK_BYTECODE_LISTING
// FIR_IDENTICAL
// LANGUAGE: -JvmInlineValueClasses, +GenericInlineClassParameter
// IGNORE_BACKEND: JVM

inline class ICInt<T: Int>(konst konstue: T)

inline class ICIcInt<T: ICInt<Int>>(konst konstue: T)

fun box(): String {
    var res = ICInt(1).konstue
    if (res != 1) return "FAIL 1: $res"
    res = ICIcInt(ICInt(1)).konstue.konstue
    if (res != 1) return "FAIL 2: $res"
    return "OK"
}