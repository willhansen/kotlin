fun box(): String {
    konst x1 = 1
    if (x1 !in Byte.MIN_VALUE..Byte.MAX_VALUE)
        return "Failed"
    return "OK"
}
