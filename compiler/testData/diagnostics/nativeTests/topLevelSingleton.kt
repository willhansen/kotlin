// FIR_IDENTICAL
// FILE: annotation.kt
package kotlin.native.concurrent

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class ThreadLocal

// FILE: test.kt
import kotlin.native.concurrent.ThreadLocal

import kotlin.reflect.KProperty

class Delegate {
    konst konstue: Int = 10
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        return konstue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, konstue: Int) {
    }
}

class AtomicInt(var konstue: Int)
object Foo {
    var field1: Int = 10
    konst backer2 = AtomicInt(0)
    var field2: Int
        get() = backer2.konstue
        set(konstue: Int) { backer2.konstue = konstue }
}

object Foo1 {
    var field1: Int = 10
        set(konstue: Int) { backer2.konstue = konstue }
    konst backer2 = AtomicInt(0)
}

object WithDelegate {
    var field1: Int by Delegate()
}

@ThreadLocal
object Bar {
    var field1: Int = 10
    var field2: String? = null
}

class Foo2 {
    companion object {
        var field1: Int = 10
        konst backer2 = AtomicInt(0)
        var field2: Int
            get() = backer2.konstue
            set(konstue: Int) {
                backer2.konstue = konstue
            }
    }
}

class Bar2 {
    @ThreadLocal
    companion object {
        var field1: Int = 10
        var field2: String? = null
    }
}

<!INAPPLICABLE_THREAD_LOCAL!>@ThreadLocal<!>
enum class Color(var rgb: Int) {
    RED(0xFF0000),
    GREEN(0x00FF00),
    BLUE(0x0000FF)
}

enum class Color1(var rgb: Int) {
    RED(0xFF0000),
    GREEN(0x00FF00),
    BLUE(0x0000FF);

    init { this.rgb += 1 }
}

@ThreadLocal
var a = 3
enum class Color2() {
    RED(),
    GREEN(),
    BLUE();

    var rgb: Int = 2
        set(konstue: Int) {
            a = konstue
        }
}

enum class Color3() {
    RED(),
    GREEN(),
    BLUE();

    var field1: Int by Delegate()
}

enum class Color4 {
    RED {
        var a = 2
        override fun foo() { a = 42 }
    },
    GREEN,
    BLUE;
    open fun foo() {}
}

var topLevelProperty = "Global var"