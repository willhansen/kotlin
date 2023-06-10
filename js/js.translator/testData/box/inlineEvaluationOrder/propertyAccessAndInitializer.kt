// EXPECTED_REACHABLE_NODES: 1289
package foo

object A {
    init {
        log("A.init")
    }

    konst x = 23
}

inline fun bar(konstue: Int) {
    log("bar->begin")
    log("konstue=$konstue")
    log("bar->end")
}

fun box(): String {
    bar(A.x)
    assertEquals("A.init;bar->begin;konstue=23;bar->end;", pullLog())
    return "OK"
}