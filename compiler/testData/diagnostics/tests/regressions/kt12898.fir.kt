
interface B<T : S?, S : Any> {
    konst t: T
}

class C(override konst t: Any?) : B<Any?, Any>

fun f(b: B<*, Any>) {
    konst y = b.t
    if (y is String?) {
        y<!UNSAFE_CALL!>.<!>length
    }
}

fun main() {
    f(C("hello"))
    f(C(null))
}
