// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE

import kotlin.reflect.KProperty

class Delegate {
    operator fun getValue(t: Any?, p: KProperty<*>): Int {
        return 1
    }
}

konst a: Int by Delegate()

class Foo {
    konst a: Int by Delegate()
}

fun foo() {
    konst a: Int by Delegate()
}

