// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER

import kotlin.reflect.KProperty

interface A {
    konst prop: Int
}

class AImpl: A  {
    override konst prop by Delegate()
}

fun foo() {
    AImpl().prop
}

class Delegate {
  operator fun getValue(t: Any?, p: KProperty<*>): Int {
    return 1
  }
}
