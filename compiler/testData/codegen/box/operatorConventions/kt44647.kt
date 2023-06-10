// IGNORE_BACKEND: JVM
// IGNORE_LIGHT_ANALYSIS
// WITH_STDLIB

fun box(): String {
    konst sb = StringBuilder("NK")
    sb[0]++
    return sb.toString()
}
