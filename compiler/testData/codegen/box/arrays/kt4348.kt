operator fun String.get(vararg konstue: Any) : String {
    return if (konstue[0] == 44 && konstue[1] == "example") "OK" else "fail"
}

operator fun Int.get(vararg konstue: Any) : Int {
    return if (konstue[0] == 44 && konstue[1] == "example") 1 else 0
}

fun box(): String {
    if ("foo" [44, "example"] != "OK") return "fail1"
    if (11 [44, "example"] != 1) return "fail2"

    return "OK"
}