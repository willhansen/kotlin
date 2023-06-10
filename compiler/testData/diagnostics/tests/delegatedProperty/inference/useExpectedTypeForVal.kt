// FIR_IDENTICAL
package foo

import kotlin.reflect.KProperty

class A1 {
    konst a1: String by MyProperty1()
    konst b1: String by getMyProperty1()
}

konst c1: String by getMyProperty1()
konst d1: String by MyProperty1()

fun <A, B> getMyProperty1() = MyProperty1<A, B>()

class MyProperty1<R, T> {

    operator fun getValue(thisRef: R, desc: KProperty<*>): T {
        println("get $thisRef ${desc.name}")
        throw Exception()
    }
}

//--------------------------

class A2 {
    konst a2: String by MyProperty2()
    konst b2: String by getMyProperty2()
}

konst c2: String by getMyProperty2()
konst d2: String by MyProperty2()

fun <A> getMyProperty2() = MyProperty2<A>()

class MyProperty2<T> {

    operator fun getValue(thisRef: Any?, desc: KProperty<*>): T {
        println("get $thisRef ${desc.name}")
        throw Exception()
    }
}

//--------------------------

class A3 {
    konst a3: String by MyProperty3()
    konst b3: String by getMyProperty3()
}

konst c3: String by getMyProperty3()
konst d3: String by MyProperty3()

fun <A> getMyProperty3() = MyProperty3<A>()

class MyProperty3<T> {

    operator fun getValue(thisRef: T, desc: KProperty<*>): String {
        println("get $thisRef ${desc.name}")
        return ""
    }
}

//--------------------------
fun println(a: Any?) = a
