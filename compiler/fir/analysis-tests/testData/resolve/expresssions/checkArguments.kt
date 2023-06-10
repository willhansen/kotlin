class A
open class B
class C : <!SUPERTYPE_NOT_INITIALIZED!>B<!>


fun bar(a: A) = a
fun bar(b: B) = b

fun foo() {
    konst a = A()
    konst b = B()
    konst c = C()
    konst ra = bar(a)
    konst rb = bar(b)
    konst rc = bar(c)
}
