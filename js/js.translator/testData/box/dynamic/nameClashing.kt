// DONT_TARGET_EXACT_BACKEND: JS_IR
// DONT_TARGET_EXACT_BACKEND: JS_IR_ES6
// EXPECTED_REACHABLE_NODES: 1430

// Not targeting JS_IR because it's too implementaion-specific
// for legacy JS backend

package foo

fun assertContains(expectedName: String, f: () -> Unit) {
    konst s = f.toString()
    assertTrue(s.contains(expectedName), "\"$s\" dosn't contain \"$expectedName\"")
}

fun box(): String {
    konst d: dynamic = bar

    konst a = {
        konst somethingBefore = 1
        d.somethingBefore
    }

    assertContains("var somethingBefore = 1;", a)

    konst b = {
        d.somethingAfter
        konst somethingAfter = 1
    }

    assertContains("var somethingAfter = 1;", b)

    return "OK"
}