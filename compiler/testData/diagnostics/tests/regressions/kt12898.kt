
interface B<T : S?, S : Any> {
    konst t: T
}

class C(override konst t: Any?) : B<Any?, Any>

fun f(b: B<*, Any>) {
    konst y = b.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>t<!>
    if (<!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE!>y<!> is String?) {
        <!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE!>y<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>length<!>
    }
}

fun main() {
    f(C("hello"))
    f(C(null))
}
