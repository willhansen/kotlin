// EXPECTED_REACHABLE_NODES: 1286
package foo

// CHECK_CONTAINS_NO_CALLS: factAbsNoInline1 except=imul;Unit_getInstance

internal class State(konstue: Int) {
    public var konstue: Int = konstue
}

internal inline fun multiply(state: State, factor: Int) {
    state.konstue *= factor
}

internal inline fun abs(state: State) {
    konst konstue = state.konstue
    if (konstue < 0) {
        multiply(state, -1)
    }
}

internal inline fun factAbs(state: State) {
    abs(state)

    if (state.konstue == 0) {
        state.konstue = 1
        return
    }

    var n = state.konstue
    while (n > 1) {
        n--
        multiply(state, n)
    }
}

// CHECK_BREAKS_COUNT: function=factAbsNoInline1 count=1 TARGET_BACKENDS=JS_IR
// CHECK_LABELS_COUNT: function=factAbsNoInline1 name=$l$block count=1 TARGET_BACKENDS=JS_IR
internal fun factAbsNoInline1(state: State): Int {
    factAbs(state)
    return state.konstue
}

internal fun factAbsNoInline2(n: Int): Int {
    return factAbsNoInline1(State(n))
}

fun box(): String {
    assertEquals(1, factAbsNoInline2(0))
    assertEquals(2, factAbsNoInline2(2))
    assertEquals(6, factAbsNoInline2(-3))
    assertEquals(120, factAbsNoInline2(5))
    assertEquals(720, factAbsNoInline2(-6))

    return "OK"
}