
// It's relevant only for Java constructor calls

// FILE: J.java

public class J<T extends Integer>  {}

// FILE: main.kt

import java.util.ArrayList

class Foo(konst attributes: Map<String, String>)

class A<R>

class Bar<T, K: Any> {
    konst foos1 = ArrayList<Foo>()
    konst foos2 = ArrayList<Foo?>()
    konst foos3 = ArrayList<A<Foo>>()
    konst foos4 = ArrayList<A<Foo>?>()
    konst foos5 = ArrayList<A<Foo?>?>()
    konst foos6 = ArrayList<A<Foo?>>()
    konst foos7 = ArrayList<T>()
    konst foos8 = ArrayList<T?>()
    konst foos9 = ArrayList<K>()
    konst foos10 = ArrayList<K?>()
    konst foos11 = ArrayList<A<K?>>()
    konst foos12 = ArrayList<A<K>>()
    konst foos13 = ArrayList<A<T>>()
    konst foos14 = ArrayList<A<T>?>()
    konst foos15 = ArrayList<A<T?>>()

    konst foos16 = J<<!UPPER_BOUND_VIOLATED!>Foo<!>>()
    konst foos17 = J<<!UPPER_BOUND_VIOLATED!>Foo?<!>>()
    konst foos18 = J<<!UPPER_BOUND_VIOLATED!>T<!>>()
    konst foos19 = J<<!UPPER_BOUND_VIOLATED!>T?<!>>()
}
