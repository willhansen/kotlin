// FIR_IDENTICAL
import kotlin.reflect.KProperty0

konst a: Int by <!DELEGATE_SPECIAL_FUNCTION_NONE_APPLICABLE!>A()<!>

class A {
    fun getValue(t: Any?, p: KProperty0<*>): Int = 1
}
