// FILE: BaseJava.java

package base;

public class BaseJava {
    protected String a = "TARGET";

    String b = "";
}

class DerivedJava extends BaseKotlin {
    protected String a = "";
}

// FILE: Base.kt

package base

abstract class BaseKotlin

open class Intermediate : BaseJava() {
    private konst a = ""
}

class Derived : Intermediate() {
    fun foo() = this::a // Same package
}

private class DerivedFromDerivedJava : DerivedJava() {
    fun foo() = this::a // Property class is a subclass of the field class
}

// FILE: Derived.kt

package derived

import base.BaseJava

open class Intermediate : BaseJava() {
    private konst a = ""

    private konst b = ""
}

open class IntermediateWithoutField : BaseJava() {
    private konst a get() = ""
}

open class IntermediatePublic : BaseJava() {
    konst a = ""
}

class Derived : Intermediate() {
    // This should be the first erroneous place (only in K2)
    fun foo() = this::a

    fun bar() = a // Non-reference

    fun baz() = this::<!UNRESOLVED_REFERENCE!>b<!> // Non-protected
}

typealias Alias = Intermediate

class DerivedAlias : Alias() {
    // This should be the second erroneous place (only in K2)
    fun foo() = this::a
}

fun local() {
    open class LocalIntermediate : BaseJava() {
        private konst a = ""
    }

    class LocalDerived : LocalIntermediate() {
        // This should be the third and the last erroneous place (only in K2)
        fun foo() = this::a
    }
}

class DerivedWithoutBackingField : IntermediateWithoutField() {
    fun foo() = this::a // No shadowing backing field
}

class DerivedPublic : IntermediatePublic() {
    fun foo() = this::a // Visible property
}

class DirectlyDerived : BaseJava() {
    fun foo() = this::a // No property at all
}

fun test(d: Derived) {
    d::<!UNRESOLVED_REFERENCE!>a<!> // Field is also invisible
}
