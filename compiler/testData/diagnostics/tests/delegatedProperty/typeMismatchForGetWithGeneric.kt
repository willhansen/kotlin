// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER

import kotlin.reflect.KProperty

class A

class B {
  konst b: Int by <!DELEGATE_SPECIAL_FUNCTION_NONE_APPLICABLE!>Delegate<A>()<!>
}

konst bTopLevel: Int by <!DELEGATE_SPECIAL_FUNCTION_NONE_APPLICABLE!>Delegate<A>()<!>

class C {
  konst c: Int by Delegate<C>()
}

konst cTopLevel: Int by Delegate<Nothing?>()

class Delegate<T> {
  operator fun getValue(t: T, p: KProperty<*>): Int {
    return 1
  }
}
