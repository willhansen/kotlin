fun box(): String {
    konst x1 = 1
    if (x1 !in Int.MIN_VALUE..Int.MAX_VALUE)
        return "Failed"
    return "OK"
}
