class A<in T, out K>
class B

fun test() {
    konst a1 = A<<!PROJECTION_ON_NON_CLASS_TYPE_ARGUMENT!>in Int<!>, <!PROJECTION_ON_NON_CLASS_TYPE_ARGUMENT!>out B<!>>()
    konst a2 = A<Int, B>()
    konst a3 = A<<!PROJECTION_ON_NON_CLASS_TYPE_ARGUMENT!>*<!>, <!PROJECTION_ON_NON_CLASS_TYPE_ARGUMENT!>*<!>>()
}
