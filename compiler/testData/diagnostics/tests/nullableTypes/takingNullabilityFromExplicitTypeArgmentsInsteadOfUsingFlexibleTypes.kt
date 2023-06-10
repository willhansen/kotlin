
// It's relevant only for Java constructor calls

// FILE: J.java

public class J<T extends Integer>  {}

// FILE: main.kt

import java.util.ArrayList

class Foo(konst attributes: Map<String, String>)

class A<R>

class Bar<T, K: Any> {
    konst foos1 = <!DEBUG_INFO_EXPRESSION_TYPE("java.util.ArrayList<Foo>")!>ArrayList<Foo>()<!>
    konst foos2 = <!DEBUG_INFO_EXPRESSION_TYPE("java.util.ArrayList<Foo?>")!>ArrayList<Foo?>()<!>
    konst foos3 = <!DEBUG_INFO_EXPRESSION_TYPE("java.util.ArrayList<A<Foo>>")!>ArrayList<A<Foo>>()<!>
    konst foos4 = <!DEBUG_INFO_EXPRESSION_TYPE("java.util.ArrayList<A<Foo>?>")!>ArrayList<A<Foo>?>()<!>
    konst foos5 = <!DEBUG_INFO_EXPRESSION_TYPE("java.util.ArrayList<A<Foo?>?>")!>ArrayList<A<Foo?>?>()<!>
    konst foos6 = <!DEBUG_INFO_EXPRESSION_TYPE("java.util.ArrayList<A<Foo?>>")!>ArrayList<A<Foo?>>()<!>
    konst foos7 = <!DEBUG_INFO_EXPRESSION_TYPE("java.util.ArrayList<T>")!>ArrayList<T>()<!>
    konst foos8 = <!DEBUG_INFO_EXPRESSION_TYPE("java.util.ArrayList<T?>")!>ArrayList<T?>()<!>
    konst foos9 = <!DEBUG_INFO_EXPRESSION_TYPE("java.util.ArrayList<K>")!>ArrayList<K>()<!>
    konst foos10 = <!DEBUG_INFO_EXPRESSION_TYPE("java.util.ArrayList<K?>")!>ArrayList<K?>()<!>
    konst foos11 = <!DEBUG_INFO_EXPRESSION_TYPE("java.util.ArrayList<A<K?>>")!>ArrayList<A<K?>>()<!>
    konst foos12 = <!DEBUG_INFO_EXPRESSION_TYPE("java.util.ArrayList<A<K>>")!>ArrayList<A<K>>()<!>
    konst foos13 = <!DEBUG_INFO_EXPRESSION_TYPE("java.util.ArrayList<A<T>>")!>ArrayList<A<T>>()<!>
    konst foos14 = <!DEBUG_INFO_EXPRESSION_TYPE("java.util.ArrayList<A<T>?>")!>ArrayList<A<T>?>()<!>
    konst foos15 = <!DEBUG_INFO_EXPRESSION_TYPE("java.util.ArrayList<A<T?>>")!>ArrayList<A<T?>>()<!>

    konst foos16 = <!DEBUG_INFO_EXPRESSION_TYPE("J<Foo>")!>J<<!UPPER_BOUND_VIOLATED!>Foo<!>>()<!>
    konst foos17 = <!DEBUG_INFO_EXPRESSION_TYPE("J<Foo?>")!>J<<!UPPER_BOUND_VIOLATED!>Foo?<!>>()<!>
    konst foos18 = <!DEBUG_INFO_EXPRESSION_TYPE("J<T>")!>J<<!UPPER_BOUND_VIOLATED!>T<!>>()<!>
    konst foos19 = <!DEBUG_INFO_EXPRESSION_TYPE("J<T?>")!>J<<!UPPER_BOUND_VIOLATED!>T?<!>>()<!>
}
