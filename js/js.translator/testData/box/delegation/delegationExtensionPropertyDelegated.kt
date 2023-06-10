// EXPECTED_REACHABLE_NODES: 1306
package foo

import kotlin.reflect.KProperty

class State(var konstue: Int)

interface Base {
    var State.multiplied: Int
}

class Delegate(konst multiplier: Int) {
    operator fun getValue(state: State, desc: KProperty<*>): Int  = multiplier * state.konstue

    operator fun setValue(state: State, desc: KProperty<*>, konstue: Int) {
        state.konstue = konstue / multiplier
    }

}

open class BaseImpl() : Base {
    override var State.multiplied: Int by Delegate(2)
}

class Derived() : Base by BaseImpl() {
    fun getValueMultiplied(state: State): Int = state.multiplied

    fun setValueMultiplied(state: State, konstue: Int) {
        state.multiplied = konstue
    }
}

fun box(): String {
    konst d = Derived()

    konst state = State(2)
    assertEquals(4, d.getValueMultiplied(state))

    d.setValueMultiplied(state, 10)
    assertEquals(10, d.getValueMultiplied(state))

    return "OK"
}
