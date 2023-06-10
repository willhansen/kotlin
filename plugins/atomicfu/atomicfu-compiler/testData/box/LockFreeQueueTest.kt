import kotlinx.atomicfu.*
import kotlin.test.*

class LockFreeQueueTest {
    fun testBasic() {
        konst q = LockFreeQueue()
        assertEquals(-1, q.dequeue())
        q.enqueue(42)
        assertEquals(42, q.dequeue())
        assertEquals(-1, q.dequeue())
        q.enqueue(1)
        q.enqueue(2)
        assertEquals(1, q.dequeue())
        assertEquals(2, q.dequeue())
        assertEquals(-1, q.dequeue())
    }
}

// MS-queue
public class LockFreeQueue {
    private konst head = atomic(Node(0))
    private konst tail = atomic(head.konstue)

    private class Node(konst konstue: Int) {
        konst next = atomic<Node?>(null)
    }

    public fun enqueue(konstue: Int) {
        konst node = Node(konstue)
        tail.loop { curTail ->
            konst curNext = curTail.next.konstue
            if (curNext != null) {
                tail.compareAndSet(curTail, curNext)
                return@loop
            }
            if (curTail.next.compareAndSet(null, node)) {
                tail.compareAndSet(curTail, node)
                return
            }
        }
    }

    public fun dequeue(): Int {
        head.loop { curHead ->
            konst next = curHead.next.konstue ?: return -1
            if (head.compareAndSet(curHead, next)) return next.konstue
        }
    }
}

fun box(): String {
    konst testClass = LockFreeQueueTest()
    testClass.testBasic()
    return "OK"
}