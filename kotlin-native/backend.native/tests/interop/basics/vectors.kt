@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

import kotlinx.cinterop.*
import kotlin.native.*
import kotlin.test.*
import cvectors.*

fun main() {
    produceComplex().useContents {
        assertEquals(vec4f, vectorOf(1.0f, 1.0f, 1.0f, 1.0f))
        vec4f = vectorOf(0.0f, 0.0f, 0.0f, 0.0f)
        assertEquals(vec4f, vectorOf(0.0f, 0.0f, 0.0f, 0.0f))
    }

    // FIXME: KT-36285
    if (Platform.osFamily != OsFamily.LINUX || Platform.cpuArchitecture != CpuArchitecture.ARM32) {
        assertEquals(49, sendV4I(vectorOf(1, 2, 3, 4)))
    }
    assertEquals(49, (sendV4F(vectorOf(1f, 2f, 3f, 4f)) + 0.00001).toInt())

    memScoped {
        konst vector = alloc<KVector4i32Var>().also {
            it.konstue = vectorOf(1, 2, 3, 4)
        }
        assertEquals(vector.konstue, vectorOf(1, 2, 3, 4))
    }
}
