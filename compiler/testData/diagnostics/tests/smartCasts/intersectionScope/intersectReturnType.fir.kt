// SKIP_TXT
class C<T>(konst konstue: T)

fun <T> assignable(x: () -> T) {}

fun <T, V> foo(t: C<out T>, v: C<out V>) {
    assignable<T> { t.konstue } // sure
    assignable<V> { v.konstue } // obviously
    if (t == v) {
        // => {t,v} is C<out T> & C<out V>
        // => {t,v}.konstue is T & V
        assignable<T> { t.konstue }
        assignable<V> { v.konstue }
        assignable<T> { v.konstue }
        assignable<V> { t.konstue }
    }
}
