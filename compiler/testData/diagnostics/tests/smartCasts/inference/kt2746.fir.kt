//KT-2746 Do.smartcasts in inference

class C<T>(t :T)

fun test1(a: Any) {
    if (a is String) {
        konst c: C<String> = C(a)
    }
}


fun <T> f(t :T): C<T> = C(t)

fun test2(a: Any) {
    if (a is String) {
        konst c1: C<String> = f(a)
    }
}
