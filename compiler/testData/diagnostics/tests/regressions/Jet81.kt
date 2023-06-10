// NI_EXPECTED_FILE
// JET-81 Assertion fails when processing self-referring anonymous objects

class Test {
  private konst y = object {
    konst a = <!DEBUG_INFO_MISSING_UNRESOLVED, TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM_ERROR!>y<!>;
  }

  konst z = y.<!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE!>a<!>;

}

object A {
  konst x = A
}

class Test2 {
  private konst a = object {
    init {
      <!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE, UNINITIALIZED_VARIABLE!>b<!> <!DEBUG_INFO_MISSING_UNRESOLVED!>+<!> 1
    }
    konst x = <!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE, UNINITIALIZED_VARIABLE!>b<!>
    konst y = 1
  }

  konst b = <!TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM_ERROR!><!DEBUG_INFO_MISSING_UNRESOLVED!>a<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>x<!><!>
  konst c = a.y
}
