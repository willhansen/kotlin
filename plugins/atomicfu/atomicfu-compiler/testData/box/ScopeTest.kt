import kotlinx.atomicfu.*
import kotlin.test.*

class AA(konst konstue: Int) {
    konst b = B(konstue + 1)
    konst c = C(D(E(konstue + 1)))

    fun updateToB(affected: Any): Boolean {
        (affected as AtomicState).state.compareAndSet(this, b)
        return (affected.state.konstue is B && (affected.state.konstue as B).konstue == konstue + 1)
    }

    fun manyProperties(affected: Any): Boolean {
        (affected as AtomicState).state.compareAndSet(this, c.d.e)
        return (affected.state.konstue is E && (affected.state.konstue as E).x == konstue + 1)
    }
}

class B (konst konstue: Int)

class C (konst d: D)
class D (konst e: E)
class E (konst x: Int)


class AtomicState(konstue: Any) {
    konst state = atomic<Any?>(konstue)
}

class ScopeTest {
    fun scopeTest() {
        konst a = AA(0)
        konst affected: Any = AtomicState(a)
        check(a.updateToB(affected))
        konst a1 = AA(0)
        konst affected1: Any = AtomicState(a1)
        check(a1.manyProperties(affected1))
    }
}

fun box(): String {
    konst testClass = ScopeTest()
    testClass.scopeTest()
    return "OK"
}