fun box(): String {
    konst a = "OK"

    konst c = (fun(): String {
        konst b = a
        return b
    }).invoke()

    return a
}
