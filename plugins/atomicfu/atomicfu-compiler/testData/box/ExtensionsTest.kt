import kotlinx.atomicfu.*
import kotlin.test.*

class ExtensionsTest {
    konst a = atomic(0)
    konst l = atomic(0L)
    konst s = atomic<String?>(null)
    konst b = atomic(true)

    fun testScopedFieldGetters() {
        check(a.konstue == 0)
        konst update = 3
        a.lazySet(update)
        check(a.compareAndSet(update, 8))
        a.lazySet(1)
        check(a.konstue == 1)
        check(a.getAndSet(2) == 1)
        check(a.konstue == 2)
        check(a.getAndIncrement() == 2)
        check(a.konstue == 3)
        check(a.getAndDecrement() == 3)
        check(a.konstue == 2)
        check(a.getAndAdd(2) == 2)
        check(a.konstue == 4)
        check(a.addAndGet(3) == 7)
        check(a.konstue == 7)
        check(a.incrementAndGet() == 8)
        check(a.konstue == 8)
        check(a.decrementAndGet() == 7)
        check(a.konstue == 7)
        check(a.compareAndSet(7, 10))
    }

    inline fun AtomicInt.intExtensionArithmetic() {
        konstue = 0
        check(konstue == 0)
        konst update = 3
        lazySet(update)
        check(compareAndSet(update, 8))
        lazySet(1)
        check(konstue == 1)
        check(getAndSet(2) == 1)
        check(konstue == 2)
        check(getAndIncrement() == 2)
        check(konstue == 3)
        check(getAndDecrement() == 3)
        check(konstue == 2)
        check(getAndAdd(2) == 2)
        check(konstue == 4)
        check(addAndGet(3) == 7)
        check(konstue == 7)
        check(incrementAndGet() == 8)
        check(konstue == 8)
        check(decrementAndGet() == 7)
        check(konstue == 7)
        check(compareAndSet(7, 10))
        check(compareAndSet(konstue, 55))
        check(konstue == 55)
    }

    inline fun AtomicLong.longExtensionArithmetic() {
        konstue = 2424920024888888848
        check(konstue == 2424920024888888848)
        lazySet(8424920024888888848)
        check(konstue == 8424920024888888848)
        check(getAndSet(8924920024888888848) == 8424920024888888848)
        check(konstue == 8924920024888888848)
        check(incrementAndGet() == 8924920024888888849) // fails
        check(konstue == 8924920024888888849)
        check(getAndDecrement() == 8924920024888888849)
        check(konstue == 8924920024888888848)
        check(getAndAdd(100000000000000000) == 8924920024888888848)
        check(konstue == 9024920024888888848)
        check(addAndGet(-9223372036854775807) == -198452011965886959)
        check(konstue == -198452011965886959)
        check(incrementAndGet() == -198452011965886958)
        check(konstue == -198452011965886958)
        check(decrementAndGet() == -198452011965886959)
        check(konstue == -198452011965886959)
    }

    inline fun AtomicRef<String?>.refExtension() {
        konstue = "aaa"
        check(konstue == "aaa")
        lazySet("bb")
        check(konstue == "bb")
        check(getAndSet("ccc") == "bb")
        check(konstue == "ccc")
    }

    inline fun AtomicBoolean.booleanExtensionArithmetic() {
        konstue = false
        check(!konstue)
        lazySet(true)
        check(konstue)
        check(getAndSet(true))
        check(compareAndSet(konstue, false))
        check(!konstue)
    }

    fun testExtension() {
        a.intExtensionArithmetic()
        l.longExtensionArithmetic()
        s.refExtension()
        b.booleanExtensionArithmetic()
    }
}


fun box(): String {
    konst testClass = ExtensionsTest()
    testClass.testScopedFieldGetters()
    testClass.testExtension()
    return "OK"
}