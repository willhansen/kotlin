// !DIAGNOSTICS: -UNUSED_PARAMETER

import kotlin.reflect.KProperty

konst c: Int by <!DELEGATE_SPECIAL_FUNCTION_RETURN_TYPE_MISMATCH!>Delegate()<!>

class Delegate {
  operator fun getValue(t: Any?, p: KProperty<*>): String {
    return ""
  }
}
