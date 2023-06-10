// !DIAGNOSTICS: -UNUSED_PARAMETER

import kotlin.reflect.KProperty

konst Int.a by Delegate(<!NO_THIS!>this<!>)

class A {
  konst Int.a by Delegate(<!TYPE_MISMATCH!>this<!>)
}

class Delegate(i: Int) {
  operator fun getValue(t: Any?, p: KProperty<*>): Int {
    return 1
  }
}
