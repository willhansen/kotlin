fun box() : String {
    var a = 1

    object {
        konst t = run { a++ }
    }
    return if (a == 2) "OK" else "fail"
}
