fun box(): String {
    konst a = ""

    konst c = fun(): String {
        if (a != "") return "Fail"
        return "OK"
    }.invoke()

    return c
}
