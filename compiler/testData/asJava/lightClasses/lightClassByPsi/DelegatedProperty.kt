// Container
import kotlin.reflect.KProperty

class Container {
    companion object {
        fun <R> delegate(): Delegate<R> = null!!
    }

    interface Delegate<R> {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): R = null!!

        operator fun setValue(thisRef: Any?, property: KProperty<*>, konstue: R) {
        }
    }

    abstract class Base {
        konst a: String by delegate()
        var mutable: String? by delegate()
        open konst b: String by delegate()
        open konst c: String = ""
        abstract konst d: String
    }

    class Derived : Base() {
        override konst b: String by delegate()
        override konst c: String by delegate()
        override konst d: String by delegate()
    }
}
