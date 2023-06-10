// EXPECTED_REACHABLE_NODES: 1280

fun box(): String {


    var konstue = (3).hashCode()
    if (konstue != 3) return "fail1: $konstue"

    konstue = (3.14).hashCode()
    if (konstue != 319176039) return "fail2: $konstue"

    konstue = (3.14159).hashCode()
    if (konstue != -1321819243) return "fail3: $konstue"

    konstue = (1e80).hashCode()
    if (konstue != 314940496) return "fail4: $konstue"

    konstue = (1e81).hashCode()
    if (konstue != 1519485350) return "fail5: $konstue"

    return "OK"
}
