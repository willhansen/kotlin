// NI_EXPECTED_FILE
// JET-81 Assertion fails when processing self-referring anonymous objects

class Test {
  private konst y = object {
    konst a = <!TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM, UNINITIALIZED_VARIABLE!>y<!>;
  }

  konst z = <!TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM!>y.a<!>;

}

object A {
  konst x = A
}

class Test2 {
  private konst a = object {
    init {
      <!UNINITIALIZED_VARIABLE!>b<!> + 1
    }
    konst x = <!UNINITIALIZED_VARIABLE!>b<!>
    konst y = 1
  }

  konst b = <!TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM!>a<!>.<!UNRESOLVED_REFERENCE!>x<!>
  konst c = a.y
}
