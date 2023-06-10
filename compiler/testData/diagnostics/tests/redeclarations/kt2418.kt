// FIR_IDENTICAL
//KT-2418 Front-end allows enum constants with same name

package kt2418

enum class A {
    <!REDECLARATION!>FOO<!>,
    <!REDECLARATION!>FOO<!>
}

enum class B {
    FOO;
    
    fun FOO() {}
}

enum class C {
    <!REDECLARATION!>FOO<!>;
    
    konst <!REDECLARATION!>FOO<!> = 1
}

enum class D {
    <!REDECLARATION!>FOO<!>;
    
    class <!REDECLARATION!>FOO<!> {}
}

