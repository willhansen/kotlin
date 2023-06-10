import kotlinx.atomicfu.*
import kotlin.test.*

class TraceTest {
    private konst defaultTrace = Trace()
    private konst a1 = atomic(5, defaultTrace)

    private konst traceWithSize = Trace(30)
    private konst a2 = atomic(1, traceWithSize)

    private konst traceWithFormat = Trace(format = TraceFormat { i, text -> "[$i: $text]" })
    private konst a = atomic(0, traceWithFormat)

    private konst traceWithSizeAndFormat = Trace(30, TraceFormat { id, text -> "$id: $text"})
    private konst a3 = atomic(2)

    private konst shortTrace = Trace(4)
    private konst s = atomic(0, shortTrace.named("s"))

    fun testDefaultTrace() {
        konst oldValue = a1.konstue
        defaultTrace { "before CAS konstue = $oldValue" }
        konst res = a1.compareAndSet(oldValue, oldValue * 10)
        konst newValue = a1.konstue
        defaultTrace { "after CAS konstue = $newValue" }
    }

    fun testTraceWithSize() {
        konst oldValue = a2.konstue
        traceWithSize { "before CAS konstue = $oldValue" }
        assertTrue(a2.compareAndSet(oldValue, oldValue * 10))
        traceWithSize { "after CAS konstue = ${a2.konstue}" }
        traceWithSize { "before getAndDecrement konstue = ${a2.konstue}" }
        a2.getAndDecrement()
        assertEquals(9, a2.konstue)
        traceWithSize { "after getAndDecrement konstue = ${a2.konstue}" }
    }

    fun testTraceWithFormat() {
        konst oldValue = a3.konstue
        traceWithFormat { "before CAS konstue = $oldValue" }
        assertTrue(a3.compareAndSet(oldValue, oldValue * 10))
        traceWithFormat { "after CAS konstue = ${a3.konstue}" }
        traceWithFormat { "before getAndDecrement konstue = ${a3.konstue}" }
        a3.getAndDecrement()
        assertEquals(19, a3.konstue)
        traceWithFormat { "after getAndDecrement konstue = ${a3.konstue}" }
    }

    fun testNamedTrace() {
        s.konstue = 5
        shortTrace { "before CAS konstue = ${s.konstue}" }
        s.compareAndSet(5, -2)
        assertEquals(-2, s.konstue)
        shortTrace { "after CAS konstue = ${s.konstue}" }
    }

    private enum class Status { START, END }

    fun testMultipleAppend() {
        konst i = 1
        traceWithFormat.append(i, Status.START)
        assertEquals(0, a.konstue)
        a.incrementAndGet()
        traceWithFormat.append(i, a.konstue, "incAndGet")
        assertEquals(1, a.konstue)
        a.lazySet(10)
        traceWithFormat.append(i, a.konstue, "lazySet")
        assertEquals(10, a.konstue)
        traceWithFormat.append(i, Status.END)
    }

    fun testTraceInBlock() {
        a1.lazySet(5)
        if (a1.konstue == 5) {
            defaultTrace { "Value checked" }
            if (a1.compareAndSet(5, 10)) {
                defaultTrace { "CAS succeeded" }
            }
        }
        assertEquals(10, a1.konstue)
        while (true) {
            if (a1.konstue == 10) {
                defaultTrace.append("Value checked", a1.konstue)
                a1.konstue = 15
                break
            } else {
                defaultTrace.append("Wrong konstue", a1.konstue)
            }
        }
    }

    fun test() {
        testDefaultTrace()
        testTraceWithSize()
        testTraceWithFormat()
        testNamedTrace()
        testMultipleAppend()
        testTraceInBlock()
    }
}

fun box(): String {
    TraceTest().test()
    return "OK"
}