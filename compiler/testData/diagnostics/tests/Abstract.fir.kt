// FILE: b.kt
package MyPackage
    //properties
    <!MUST_BE_INITIALIZED!>konst a: Int<!>
    konst a1: Int = 1
    <!WRONG_MODIFIER_TARGET!>abstract<!> konst a2: Int
    <!WRONG_MODIFIER_TARGET!>abstract<!> konst a3: Int = 1

    <!MUST_BE_INITIALIZED!>var b: Int<!>                private set
    var b1: Int = 0;                         private set
    <!WRONG_MODIFIER_TARGET!>abstract<!> var b2: Int      private set
    <!WRONG_MODIFIER_TARGET!>abstract<!> var b3: Int = 0; private set

    <!MUST_BE_INITIALIZED!>var c: Int<!>                set(v: Int) { field = v }
    var c1: Int = 0;                         set(v: Int) { field = v }
    <!WRONG_MODIFIER_TARGET!>abstract<!> var c2: Int      set(v: Int) { field = v }
    <!WRONG_MODIFIER_TARGET!>abstract<!> var c3: Int = 0; set(v: Int) { field = v }

    konst e: Int                               get() = a
    konst e1: Int = <!PROPERTY_INITIALIZER_NO_BACKING_FIELD!>0<!>;          get() = a
    <!WRONG_MODIFIER_TARGET!>abstract<!> konst e2: Int      get() = a
    <!WRONG_MODIFIER_TARGET!>abstract<!> konst e3: Int = 0; get() = a

    //methods
    <!NON_MEMBER_FUNCTION_NO_BODY!>fun f()<!>
    fun g() {}
    <!WRONG_MODIFIER_TARGET!>abstract<!> fun h()
    <!WRONG_MODIFIER_TARGET!>abstract<!> fun j() {}

    //property accessors
    var i: Int                       <!WRONG_MODIFIER_TARGET!>abstract<!> get  <!WRONG_MODIFIER_TARGET!>abstract<!> set
    var i1: Int = <!PROPERTY_INITIALIZER_NO_BACKING_FIELD!>0<!>;  <!WRONG_MODIFIER_TARGET!>abstract<!> get  <!WRONG_MODIFIER_TARGET!>abstract<!> set

    var j: Int                       get() = i;    <!WRONG_MODIFIER_TARGET!>abstract<!> set
    var j1: Int = <!PROPERTY_INITIALIZER_NO_BACKING_FIELD!>0<!>;  get() = i;    <!WRONG_MODIFIER_TARGET!>abstract<!> set

    <!MUST_BE_INITIALIZED!>var k: Int<!>        <!WRONG_MODIFIER_TARGET!>abstract<!> set
    var k1: Int = 0;                 <!WRONG_MODIFIER_TARGET!>abstract<!> set

    var l: Int                       <!WRONG_MODIFIER_TARGET!>abstract<!> get  <!WRONG_MODIFIER_TARGET!>abstract<!> set
    var l1: Int = <!PROPERTY_INITIALIZER_NO_BACKING_FIELD!>0<!>;  <!WRONG_MODIFIER_TARGET!>abstract<!> get  <!WRONG_MODIFIER_TARGET!>abstract<!> set

    var n: Int                       <!WRONG_MODIFIER_TARGET!>abstract<!> get <!WRONG_MODIFIER_TARGET!>abstract<!> set(v: Int) {}

// FILE: c.kt
//creating an instance
abstract class B1(
    konst i: Int,
    konst s: String
) {
}

class B2() : B1(1, "r") {}

abstract class B3(i: Int) {
}

fun foo(c: B3) {
    konst a = <!CREATING_AN_INSTANCE_OF_ABSTRACT_CLASS!>B3(1)<!>
    konst b = <!CREATING_AN_INSTANCE_OF_ABSTRACT_CLASS!>B1(2, "s")<!>
}
