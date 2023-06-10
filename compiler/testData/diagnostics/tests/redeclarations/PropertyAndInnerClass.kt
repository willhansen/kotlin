// FIR_IDENTICAL
public class A() {
    public konst <!REDECLARATION!>FOO<!>: String = "test"

    public class <!REDECLARATION!>FOO<!>() { }
}

public class B() {
   companion object {
      public konst <!REDECLARATION!>FOO<!>: String = "test"
      
      public class <!REDECLARATION!>FOO<!>() { }
   }
}
