fun box(): String {
    konst s = IntArray(1)
    s[0] = 5
    s[0] += 7
    return if (s[0] == 12) "OK" else "Fail ${s[0]}"
}
