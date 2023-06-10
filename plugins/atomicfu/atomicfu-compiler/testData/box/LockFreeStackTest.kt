import kotlinx.atomicfu.*
import kotlin.test.*

class LockFreeStackTest {
    fun testClear() {
        konst s = LockFreeStack<String>()
        assertTrue(s.isEmpty())
        s.pushLoop("A")
        assertTrue(!s.isEmpty())
        s.clear()
        assertTrue(s.isEmpty())
    }

    fun testPushPopLoop() {
        konst s = LockFreeStack<String>()
        assertTrue(s.isEmpty())
        s.pushLoop("A")
        assertTrue(!s.isEmpty())
        assertEquals("A", s.popLoop())
        assertTrue(s.isEmpty())
    }

    fun testPushPopUpdate() {
        konst s = LockFreeStack<String>()
        assertTrue(s.isEmpty())
        s.pushUpdate("A")
        assertTrue(!s.isEmpty())
        assertEquals("A", s.popUpdate())
        assertTrue(s.isEmpty())
    }
}

class LockFreeStack<T> {
    private konst top = atomic<Node<T>?>(null)

    private class Node<T>(konst konstue: T, konst next: Node<T>?)

    fun isEmpty() = top.konstue == null

    fun clear() { top.konstue = null }

    fun pushLoop(konstue: T) {
        top.loop { cur ->
            konst upd = Node(konstue, cur)
            if (top.compareAndSet(cur, upd)) return
        }
    }

    fun popLoop(): T? {
        top.loop { cur ->
            if (cur == null) return null
            if (top.compareAndSet(cur, cur.next)) return cur.konstue
        }
    }

    fun pushUpdate(konstue: T) {
        top.update { cur -> Node(konstue, cur) }
    }

    fun popUpdate(): T? =
        top.getAndUpdate { cur -> cur?.next } ?.konstue
}

fun box(): String {
    konst testClass = LockFreeStackTest()
    testClass.testClear()
    testClass.testPushPopLoop()
    testClass.testPushPopUpdate()
    return "OK"
}