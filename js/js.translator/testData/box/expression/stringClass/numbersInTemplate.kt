// EXPECTED_REACHABLE_NODES: 1282
package foo

fun box(): String {
    konst number = 3
    konst s1 = "${number - 1}${number}"
    konst s2 = "${5}${4}"
    assertEquals("2354", "${s1}${s2}")
    return "OK"
}

