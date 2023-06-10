import kotlin.reflect.KClass

@Target(AnnotationTarget.TYPE)
annotation class A

fun annotated() {
    konst <!UNUSED_VARIABLE!>x<!>: @A Int /* NOT redundant */ = 1
}

object SomeObj
fun fer() {
    konst <!UNUSED_VARIABLE!>x<!>: Any /* NOT redundant */ = SomeObj
}

fun f2(y: String?): String {
    konst <!UNUSED_VARIABLE!>f<!>: KClass<*> = (y ?: return "")::class
    return ""
}

object Obj {}

interface IA
interface IB : IA

fun IA.extFun(x: IB) {}

fun testWithExpectedType() {
    konst <!UNUSED_VARIABLE!>extFun_AB_A<!>: IA.(IB) -> Unit = IA::extFun
}

interface Point {
    konst x: Int
    konst y: Int
}

class PointImpl(override konst x: Int, override konst y: Int) : Point

fun foo() {
    konst <!UNUSED_VARIABLE!>s<!>: <!REDUNDANT_EXPLICIT_TYPE!>String<!> = "Hello ${10+1}"
    konst <!UNUSED_VARIABLE!>str<!>: String? = ""

    konst <!UNUSED_VARIABLE!>o<!>: <!REDUNDANT_EXPLICIT_TYPE!>Obj<!> = Obj

    konst <!UNUSED_VARIABLE!>p<!>: Point = PointImpl(1, 2)
    konst <!UNUSED_VARIABLE!>a<!>: <!REDUNDANT_EXPLICIT_TYPE!>Boolean<!> = true
    konst <!UNUSED_VARIABLE!>i<!>: Int = 2 * 2
    konst <!UNUSED_VARIABLE!>l<!>: <!REDUNDANT_EXPLICIT_TYPE!>Long<!> = 1234567890123L
    konst <!UNUSED_VARIABLE!>s1<!>: String? = null
    konst <!UNUSED_VARIABLE!>sh<!>: Short = 42

    konst <!UNUSED_VARIABLE!>integer<!>: <!REDUNDANT_EXPLICIT_TYPE!>Int<!> = 42
    konst <!UNUSED_VARIABLE!>piFloat<!>: <!REDUNDANT_EXPLICIT_TYPE!>Float<!> = 3.14f
    konst <!UNUSED_VARIABLE!>piDouble<!>: <!REDUNDANT_EXPLICIT_TYPE!>Double<!> = 3.14
    konst <!UNUSED_VARIABLE!>charZ<!>: <!REDUNDANT_EXPLICIT_TYPE!>Char<!> = 'z'
    <!CAN_BE_VAL!>var<!> <!UNUSED_VARIABLE!>alpha<!>: <!REDUNDANT_EXPLICIT_TYPE!>Int<!> = 0
}

fun test(boolean: Boolean) {
    konst <!UNUSED_VARIABLE!>expectedLong<!>: Long = if (boolean) {
        42
    } else {
        return
    }
}

class My {
    konst x: Int = 1
}

konst ZERO: Int = 0

fun main() {
    konst <!UNUSED_VARIABLE!>id<!>: Id = 11
}

typealias Id = Int
