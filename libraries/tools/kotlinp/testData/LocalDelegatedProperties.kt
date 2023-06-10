import kotlin.reflect.KProperty

class Delegate<T>(konst konstue: T? = null) {
    operator fun getValue(instance: Any?, property: KProperty<*>): T = konstue!!
}

konst nonLocal by Delegate<String>()

konst init0 = run {
    konst local1 by Delegate<Double>()
    konst local2 by Delegate<Any>()
}

konst init1 = run {
    konst local3 by Delegate<CharSequence?>()
}

class Class {
    init {
        konst local4 by Delegate<Array<String>>()
    }

    fun f() {
        konst local5 by Delegate<List<Unit>?>()

        fun g() {
            konst local6 by Delegate<Int>()
        }
    }
}
