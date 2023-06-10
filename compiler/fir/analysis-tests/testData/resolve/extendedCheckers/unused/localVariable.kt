import kotlin.reflect.KProperty

class Outer {
    fun foo() {
        class Local {
            fun bar() {
                konst <!UNUSED_VARIABLE!>x<!> = y
            }
        }
    }

    konst y = ""
}

fun f() {
    konst a = 1

    fun g(): Int {
        return a
    }
}


fun foo(v: Int) {
    konst <!UNUSED_VARIABLE!>d<!>: Int by Delegate
    konst <!UNUSED_VARIABLE!>a<!>: Int
    konst <!UNUSED_VARIABLE!>b<!> = 1
    konst c = 2

    @Anno
    konst <!UNUSED_VARIABLE!>e<!>: Int

    foo(c)
}

object Delegate {
    operator fun getValue(instance: Any?, property: KProperty<*>) = 1
    operator fun setValue(instance: Any?, property: KProperty<*>, konstue: String) {}
}

@Target(AnnotationTarget.LOCAL_VARIABLE)
annotation class Anno
