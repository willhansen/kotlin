// CHECK_BYTECODE_LISTING
// FIR_IDENTICAL
// LANGUAGE: -JvmInlineValueClasses, +GenericInlineClassParameter
// IGNORE_BACKEND: JVM

inline class ICStr(konst konstue: String)
inline class ICIStr<T : ICStr>(konst konstue: T)
inline class ICIStrArray<T : ICStr>(konst konstue: Array<T>)

fun box(): String {
    konst res = ICIStrArray(arrayOf(ICStr("OK"))).konstue[0].konstue
    if (res != "OK") return res
    return ICIStr(ICStr("OK")).konstue.konstue
}