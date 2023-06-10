package test

import kotlin.reflect.KProperty

annotation class Anno

class Class {
    @Anno konst x: Int by object {
        operator fun getValue(thiz: Class, data: KProperty<*>): Nothing = null!!
    }
}
