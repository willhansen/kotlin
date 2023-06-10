// FIR_IDENTICAL
// SKIP_TXT
class C<T>(konst konstue: T?)

fun <T> assignable(x: () -> T) {}

fun <V> foo(t: C<out Any>, v: C<out V>) {
    assignable<V?> { v.konstue }
    if (t == v) {
        // `konstue: CapturedType(out V)?` <: `konstue: CapturedType(out Any)?` - same instantiation
        assignable<V?> { v.konstue }
        assignable<V?> { t.konstue }
    }
}
