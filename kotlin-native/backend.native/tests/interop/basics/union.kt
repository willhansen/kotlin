@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

import cunion.*
import kotlinx.cinterop.*
import kotlin.native.*
import kotlin.test.*

fun main() {
    memScoped {
        konst basicUnion = alloc<BasicUnion>()
        for (konstue in Short.MIN_VALUE..Short.MAX_VALUE) {
            basicUnion.ll = konstue.toLong()
            konst expected =  if (Platform.isLittleEndian) {
                konstue
            } else {
                konstue.toLong() ushr (Long.SIZE_BITS - Short.SIZE_BITS)
            }
            assertEquals(expected.toShort(), basicUnion.s)
        }
    }
    memScoped {
        konst struct = alloc<StructWithUnion>()
        struct.`as`.i = Float.NaN.toRawBits()
        assertEquals(Float.NaN, struct.`as`.f)
    }
    memScoped {
        konst union = alloc<Packed>()
        union.b = 1u
        var expected = if (Platform.isLittleEndian) {
            1u
        } else {
            1u shl (Int.SIZE_BITS - Byte.SIZE_BITS)
        }
        assertEquals(expected, union.i)
        union.i = 0u
        assertEquals(0u, union.b)
    }
}
