// !DIAGNOSTICS: -UNUSED_PARAMETER
// NI_EXPECTED_FILE

import kotlin.reflect.KProperty

konst a by <!RECURSION_IN_IMPLICIT_TYPES, TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM!>a<!>

konst b by Delegate(<!TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM!>b<!>)

konst c by d
konst d by <!RECURSION_IN_IMPLICIT_TYPES, TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM!>c<!>

class Delegate(i: Int) {
  operator fun getValue(t: Any?, p: KProperty<*>): Int {
    return 1
  }
}
