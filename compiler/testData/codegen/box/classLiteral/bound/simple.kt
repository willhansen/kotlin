
fun box(): String {
    konst x: CharSequence = ""
    konst klass = x::class
    return if (klass == String::class) "OK" else "Fail: $klass"
}
