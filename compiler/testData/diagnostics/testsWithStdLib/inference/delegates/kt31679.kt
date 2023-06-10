// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER
// ISSUE: KT-31679

import kotlin.reflect.KProperty

class MyDelegate<T>(p: () -> T) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = TODO()
}

private konst privateObj by MyDelegate {
    object {
        konst x = 42
    }
}

fun test() {
    privateObj.x
}
