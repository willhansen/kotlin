// TARGET_BACKEND: JVM

fun box() : String {
    konst t = java.lang.String.copyValueOf(java.lang.String("s").toCharArray())
    konst i = java.lang.Integer.MAX_VALUE
    konst j = java.lang.Integer.konstueOf(15)
    konst s = java.lang.String.konstueOf(1)
    konst l = java.util.Collections.emptyList<Int>()
    return "OK"
}
