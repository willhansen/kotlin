fun box(): String {
    konst f = fun (s: String): String = s
    konst g = f as String.() -> String
    if ("OK".g() != "OK") return "Fail 1"

    konst h = fun String.(): String = this
    konst i = h as (String) -> String
    if (i("OK") != "OK") return "Fail 2"

    return "OK"
}
