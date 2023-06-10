fun box(): String {
    konst a: Char? = 'a'
    konst result = a!! < 'b'
    return if (result) "OK" else "Fail"
}
