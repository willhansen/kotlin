// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER

import kotlin.reflect.KProperty

konst a: Int by Delegate()

class Delegate {
  operator fun getValue(t: Any?, p: KProperty<*>): Int {
    return 1
  }
}
