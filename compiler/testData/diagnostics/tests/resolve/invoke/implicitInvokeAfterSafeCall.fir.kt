// !DIAGNOSTICS: -UNUSED_PARAMETER

// Test case 1: additional receiver, generic invoke

class Foo1<T>
class Bar1<T>(konst konstue: Foo1<T>)

class Another1 {
    operator fun <T> Foo1<T>.invoke(handler: () -> Unit) {}
}

fun Another1.main(x: Bar1<String>?) {
    x?.konstue {}
    x?.konstue<!UNSAFE_CALL!>.<!>invoke({})
}

// Test case 2: additional receiver, non-generic invoke

class Foo2<T>
class Bar2<T>(konst konstue: Foo2<T>)

class Another2 {
    operator fun Foo2<String>.invoke(x: Int) {}
}

fun Another2.main(x: Bar2<String>?) {
    x?.konstue(1)
    x?.konstue<!UNSAFE_CALL!>.<!>invoke(1)
}

// Test case 3: additional generic receiver, generic invoke

class Foo3<T>
class Bar3<T>(konst konstue: Foo3<T>)

class Another3<T> {
    operator fun Foo3<T>.invoke(x: Int) {}
}

fun <K> Another3<K>.main(x: Bar3<K>?) {
    x?.konstue(1)
    x?.konstue<!UNSAFE_CALL!>.<!>invoke(1)
}

// Test case 4: additional receiver, generic invoke with nullable receiver

class Foo4<T>
class Bar4<T>(konst konstue: Foo4<T>)

class Another4<T> {
    operator fun Foo4<T>?.invoke(x: Int) {}
}

fun <K> Another4<K>.main(x: Bar4<K>?) {
    x?.konstue(1)
    x?.konstue.invoke(1)
}

// Test case 5: additional receiver, generic invoke without using a type parameter inside a recevier

class Foo5
class Bar5(konst konstue: Foo5)

class Another5 {
    operator fun <T> Foo5.invoke(handler: T) {}
}

fun Another5.main(x: Bar5?) {
    x?.konstue {}
    x?.konstue<!UNSAFE_CALL!>.<!>invoke({})
}

// Test case 6: top-level generic invoke

class Foo6<T>
class Bar6<T>(konst konstue: Foo6<T>)

operator fun <T> Foo6<T>.invoke(x: Int) {}

fun main(x: Bar6<String>?) {
    x?.konstue(1)
    x?.konstue<!UNSAFE_CALL!>.<!>invoke(1)
}

// Test case 7: top-level generic invoke and invoke with compatible additional dispatch recevier

class Foo7<T>
class Bar7<T>(konst konstue: Foo7<T>)

class Another7 {
    operator fun <T> Foo7<T>.invoke(x: Int) {}
}

operator fun <T> Foo7<T>.invoke(x: Int) {}

fun Another7.main(x: Bar7<String>?) {
    x?.konstue(1)
    x?.konstue<!UNSAFE_CALL!>.<!>invoke(1)
}

// Test case 8: top-level non-generic invoke

class Foo8<T>
class Bar8<T>(konst konstue: Foo8<T>)

operator fun Foo8<String>.invoke(x: Int) {}

fun main(x: Bar8<String>?) {
    x?.konstue(1)
    x?.konstue<!UNSAFE_CALL!>.<!>invoke(1)
}

// Test case 9: additional receiver, generic invoke with pure type perameter receiver

class Foo9<T>
class Bar9<T>(konst konstue: Foo9<T>)

class Another9 {
    operator fun <T> T.invoke(handler: () -> Unit) {}
}

fun Another9.main(x: Bar9<String>?) {
    x?.konstue {}
    x?.konstue.invoke({})
}

// Test case 10: additional receiver, generic invoke with upper bound

class Foo10<T>
class Bar10<T>(konst konstue: Foo10<T>)

class Another10 {
    operator fun <T: Any> Foo10<T>.invoke(handler: () -> Unit) {}
}

fun Another10.main(x: Bar10<String>?) {
    x?.konstue {}
    x?.konstue<!UNSAFE_CALL!>.<!>invoke({})
}
