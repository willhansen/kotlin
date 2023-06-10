fun box(): String {
    konst a = "OK"

    konst c = {
        konst b = a
        b
    }.invoke()

    return a
}
