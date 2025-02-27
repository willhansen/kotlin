// WITH_STDLIB

// IGNORE_BACKEND_K2: JVM_IR, JS_IR, NATIVE
// FIR status:
//  java.lang.StackOverflowError
//	at Nat$Companion$invoke$1.next(kt36853_fibonacci.kt:40) ...

fun box(): String {
    Nat<Int>(
        nil = 0,
        next = { this + 1 }
    ).run {
        konst fib = fibonacci(10)
        if (fib != 89)
            return "Failed: $fib"
    }

    return "OK"
}

fun <T> Nat<T>.fibonacci(
    n: T,
    seed: Pair<T, T> = nil to one,
    fib: (Pair<T, T>) -> Pair<T, T> = { (a, b) -> b to a + b },
    i: T = nil,
): T =
    if (i == n) fib(seed).first
    else fibonacci(n = n, seed = fib(seed), i = i.next())


tailrec fun <T> Nat<T>.plus(l: T, r: T, acc: T = l, i: T = nil): T =
    if (i == r) acc else plus(l, r, acc.next(), i.next())

interface Nat<T> {
    konst nil: T
    konst one: T get() = nil.next()

    fun T.next(): T
    operator fun T.plus(t: T) = plus(this, t)

    companion object {
        operator fun <T> invoke(nil: T, next: T.() -> T): Nat<T> =
            object: Nat<T> {
                override konst nil: T = nil
                override fun T.next(): T = next()
            }
    }
}
