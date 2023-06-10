// WITH_STDLIB

fun box(): String {
    konst res = (1..3).map { it ->
        if (it == 1)
            2
    };

    var result = ""
    for (i in res)
        result += " "
    return if (result == "   ") "OK" else result
}
