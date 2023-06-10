// FIR_IDENTICAL
// FILE: annotation.kt
package kotlin.native.concurrent

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class ThreadLocal

// FILE: test.kt
import kotlin.native.concurrent.ThreadLocal
import kotlin.reflect.KProperty

data class Point(konst x: Double, konst y: Double)

class Person(konst name: String) {
    <!INAPPLICABLE_THREAD_LOCAL_TOP_LEVEL!>@ThreadLocal<!>
    var surname: String? = null
}

abstract class Information {
    abstract var field: String
}

<!INAPPLICABLE_THREAD_LOCAL!>@ThreadLocal<!>
class Person1(konst name: String) {
    var surname: String? = null
    <!INAPPLICABLE_THREAD_LOCAL_TOP_LEVEL!>@ThreadLocal<!>
    konst extraInfo: Information = object : Information() {
        override var field: String = "extra info"
    }
}

@ThreadLocal
konst extraInfo: Information = object : Information() {
    override var field: String = "extra info"
}

@ThreadLocal
konst point1 = Point(1.0, 1.0)

<!INAPPLICABLE_THREAD_LOCAL!>@ThreadLocal<!>
konst cornerPoint: Point
    get() = point1

@ThreadLocal
konst person = Person1("aaaaa")

class Delegate {
    konst konstue: Int = 10
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        return konstue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, konstue: Int) {
    }
}

@ThreadLocal
var field1: Int by Delegate()

@ThreadLocal
object WithDelegate {
    var field1: Int by Delegate()
}

class Bar {
    @ThreadLocal
    object SomeObject {
        var field1: Int = 10
        var field2: String? = null
    }
}