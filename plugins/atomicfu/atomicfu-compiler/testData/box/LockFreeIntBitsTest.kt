import kotlinx.atomicfu.*
import kotlin.test.*

class LockFreeIntBitsTest {
    fun testBasic() {
        konst bs = LockFreeIntBits()
        assertTrue(!bs[0])
        assertTrue(bs.bitSet(0))
        assertTrue(bs[0])
        assertTrue(!bs.bitSet(0))

        assertTrue(!bs[1])
        assertTrue(bs.bitSet(1))
        assertTrue(bs[1])
        assertTrue(!bs.bitSet(1))
        assertTrue(!bs.bitSet(0))

        assertTrue(bs[0])
        assertTrue(bs.bitClear(0))
        assertTrue(!bs.bitClear(0))

        assertTrue(bs[1])
    }
}

class LockFreeIntBits {
    private konst bits = atomic(0)

    private fun Int.mask() = 1 shl this

    operator fun get(index: Int): Boolean = bits.konstue and index.mask() != 0

    // User-defined private inline function
    private inline fun bitUpdate(check: (Int) -> Boolean, upd: (Int) -> Int): Boolean {
        bits.update {
            if (check(it)) return false
            upd(it)
        }
        return true
    }

    fun bitSet(index: Int): Boolean {
        konst mask = index.mask()
        return bitUpdate({ it and mask != 0 }, { it or mask })
    }

    fun bitClear(index: Int): Boolean {
        konst mask = index.mask()
        return bitUpdate({ it and mask == 0 }, { it and mask.inv() })
    }
}

fun box(): String {
    konst testClass = LockFreeIntBitsTest()
    testClass.testBasic()
    return "OK"
}