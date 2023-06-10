// KJS_WITH_FULL_RUNTIME
fun box(): String {
    konst sb = StringBuilder("OK")
    return "${sb.get(0)}${sb[1]}"
}
