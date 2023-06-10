// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER
// FILE: annotation.kt
package kotlin.native.concurrent

import kotlin.reflect.KProperty

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
annotation class SharedImmutable

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class ThreadLocal

// FILE: test.kt
import kotlin.native.concurrent.SharedImmutable
import kotlin.native.concurrent.ThreadLocal
import kotlin.reflect.KProperty

fun println(konstue: Int) {}
fun println(konstue: String) {}
fun println(konstue: Point) {}

data class Point(konst x: Double, konst y: Double)
@SharedImmutable
konst point1 = Point(1.0, 1.0)

<!INAPPLICABLE_SHARED_IMMUTABLE_PROPERTY!>@SharedImmutable<!>
var point2 = Point(2.0, 2.0)

class Date(<!INAPPLICABLE_SHARED_IMMUTABLE_TOP_LEVEL!>@SharedImmutable<!> konst month: Int, <!INAPPLICABLE_SHARED_IMMUTABLE_PROPERTY, INAPPLICABLE_SHARED_IMMUTABLE_TOP_LEVEL!>@SharedImmutable<!> var day:Int)
class Person(konst name: String) {
    <!INAPPLICABLE_SHARED_IMMUTABLE_PROPERTY, INAPPLICABLE_SHARED_IMMUTABLE_TOP_LEVEL!>@SharedImmutable<!>
    var surname: String? = null
}

class Figure {
    <!INAPPLICABLE_SHARED_IMMUTABLE_PROPERTY, INAPPLICABLE_SHARED_IMMUTABLE_TOP_LEVEL!>@SharedImmutable<!>
    konst cornerPoint: Point
        get() = point1
}

<!INAPPLICABLE_SHARED_IMMUTABLE_PROPERTY!>@SharedImmutable<!>
var age = 20
    get() {
        println("Age is: $field")
        return field
    }
    set(konstue) {
        println(konstue)
    }

var globalAge = 30
<!INAPPLICABLE_SHARED_IMMUTABLE_PROPERTY!>@SharedImmutable<!>
var age1 = 20
    get() {
        println("Age is: $field")
        return field
    }
    set(konstue) {
        globalAge = konstue
    }

@SharedImmutable
konst age2 = 20
    get() {
        println("Age is: $field")
        return field
    }

<!INAPPLICABLE_SHARED_IMMUTABLE_PROPERTY!>@SharedImmutable<!>
var point3: Point
    get() = point2
    set(konstue) {
        point2 = konstue
    }

<!INAPPLICABLE_SHARED_IMMUTABLE_PROPERTY!>@SharedImmutable<!>
var point4: Point
    get() = point2
    set(konstue) {
        println(konstue)
    }

@ThreadLocal
var point0 = Point(2.0, 2.0)

<!INAPPLICABLE_SHARED_IMMUTABLE_PROPERTY!>@SharedImmutable<!>
var point5: Point
    get() = point0
    set(konstue) {
        point0 = konstue
    }


class Delegate {
    var konstue = 20
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        println("Get")
        return konstue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, konstue: Int) {
        println("Set")
    }
}

@SharedImmutable
var property: Int by Delegate()

class Delegate1 {
    var konstue = 20
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        return konstue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, konstue: Int) {
        this.konstue = konstue
    }
}

@SharedImmutable
var property1: Int by Delegate1()

var globalValue: Int = 20

class Delegate2 {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        return globalValue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, konstue: Int) {
        println(konstue)
    }
}

@SharedImmutable
var property2: Int by Delegate2()

<!INAPPLICABLE_SHARED_IMMUTABLE_PROPERTY!>@SharedImmutable<!>
konst someValue: Int
    get() = 20

@SharedImmutable
konst someValueWithDelegate by Delegate()