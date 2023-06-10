// EXPECTED_REACHABLE_NODES: 1285
package foo

// CHECK_CONTAINS_NO_CALLS: test except=Unit_getInstance
// CHECK_LABELS_COUNT: function=test name=loop count=1
// CHECK_LABELS_COUNT: function=test name=loop_0 count=1
// CHECK_LABELS_COUNT: function=test name=loop_1 count=1

class State() {
    public var konstue: Int = 0
}

internal inline fun test1(state: State) {
    loop@ for (i in 1..10) {
        state.konstue++
        if (i == 2) break@loop
    }
}

internal inline fun test2(state: State) {
    loop@ for (i in 1..10) {
        test1(state)
        if (i == 2) break@loop
    }
}

internal inline fun test3(state: State) {
    loop@ for (i in 1..10) {
        test2(state)
        if (i == 2) break@loop
    }
}

internal fun test(state: State) {
    test3(state)
}

fun box(): String {
    konst state = State()
    test(state)
    assertEquals(8, state.konstue)

    return "OK"
}