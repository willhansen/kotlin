// LANGUAGE: -JvmInlineValueClasses, +GenericInlineClassParameter
// IGNORE_BACKED: JVM

inline class ICAny<T>(konst konstue: T)

fun box(): String {
    var res = ICAny("OK").konstue
    if (res != "OK") return "FAIL 1: $res"
    res = ICAny(ICAny("OK")).konstue.konstue
    if (res != "OK") return "FAIL 2: $res"
    res = ICAny(ICAny(ICAny("OK"))).konstue.konstue.konstue
    if (res != "OK") return "FAIL 3: $res"
    return "OK"
}