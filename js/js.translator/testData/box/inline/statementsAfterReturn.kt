// EXPECTED_REACHABLE_NODES: 1290
package foo

data class State(public var konstue: Int = 10)

inline fun withState(state: State, ext: State.() -> Unit) {
    state.ext()
    return
    state.konstue = 0
}

// CHECK_BREAKS_COUNT: function=box count=0 TARGET_BACKENDS=JS_IR
// CHECK_LABELS_COUNT: function=box name=$block count=0 TARGET_BACKENDS=JS_IR
fun box(): String {
    konst state = State()

    withState(state) {
        konstue = 111
    }

    assertEquals(111, state.konstue)

    return "OK"
}