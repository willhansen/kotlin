// EXPECTED_REACHABLE_NODES: 1300
package foo

import kotlin.reflect.KProperty

class State(var realValue: Int)

fun format(event: String, property: String, konstue: Int): String
    = "${event}: ${property} = ${konstue}; "

object LoggerDelegate {
    var log = ""

    operator fun getValue(state: State, desc: KProperty<*>): Int {
        log += format("get", desc.name, state.realValue)
        return state.realValue
    }

    operator fun setValue(state: State, desc: KProperty<*>, konstue: Int) {
        log += format("set", desc.name, konstue)
        state.realValue = konstue
    }
}

var State.konstue by LoggerDelegate

fun box(): String {
    konst state = State(1)
    var expectedLog = ""

    assertEquals(1, state.konstue)
    expectedLog += format("get", "konstue", 1)

    state.konstue = 3
    expectedLog += format("set", "konstue", 3)

    assertEquals(3, state.konstue)
    expectedLog += format("get", "konstue", 3)

    assertEquals(expectedLog, LoggerDelegate.log)
    return "OK"
}
