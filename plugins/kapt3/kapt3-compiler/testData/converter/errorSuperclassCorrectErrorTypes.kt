// CORRECT_ERROR_TYPES
// NO_VALIDATION
// WITH_STDLIB

@file:Suppress("UNRESOLVED_REFERENCE", "DELEGATION_NOT_TO_INTERFACE", "SUPERTYPE_NOT_INITIALIZED")
package test

interface Intf

open class Cl

class TFooBarBaz: Foo(), Bar, Baz

// Error, two ()
class TFooBarBaz2: Foo(), Bar(), Baz, Intf

class TFooBarBaz3 : Foo, Bar, Baz

class TFooBarBaz4() : Foo, Bar, Baz

class TFooBarBaz5() : Foo, Bar, Baz {
    constructor(s: String) {}
}

class TFooBarBaz6 : Foo, Bar, Baz {
    constructor(s: String) : super(s)
}

class TClBarBaz : Cl, Bar, Baz

class TBarBazCl : Bar, Baz, Cl

class TFooBar(konst a: X) : Foo(), Bar by a, Intf

class TFooBar2(konst a: X): Foo by a, Bar by a

class TxFooxBarxBaz : x.Foo(), x.Bar, x.Baz, Intf

// Error, two ()
class TxFooxBarxBaz2 : x.Foo(), x.Bar, x.Baz()

class Generics1 : Foo<String>()

class Generics2 : Foo<String>

class Generics3 : Foo<Bar, Baz, Boo<Baz, List<*>>, String>

class MappedList<R>() : AbstractList<R>(), List<R> {
    override fun get(index: Int) = throw RuntimeException()
    override konst size get() = 0
}

interface Parent<A : CharSequence?, B>

class Child : AbstractList<String>(), Parent<String, Int>, List<String>
