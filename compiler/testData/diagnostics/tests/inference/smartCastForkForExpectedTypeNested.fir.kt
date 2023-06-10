// SKIP_TXT

interface Slice<V>

interface A
interface B : A
interface C : A

konst SL0: Slice<A> = TODO()
konst SL1: Slice<B> = TODO()
konst SL2: Slice<C> = TODO()

fun <X> foo(s: Slice<X>): X? {
    if (s.hashCode() == 0) {
        return id(bar(s))
    }

    if (s === SL0) {
        return id(bar(s))
    }

    if (s === SL1 || s === SL2) {
        return id(bar(s))
    }
    return null
}

fun <T> id(x: T): T = x

fun <Y> bar(w: Slice<Y>): Y? = null
