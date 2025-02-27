@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlin.native.internal.InternalForKotlinNative::class)

package codegen.intrinsics.interop_sourceCodeStruct

import kotlinx.cinterop.*
import kotlinx.cinterop.internal.*
import kotlin.test.*

// Just making sure this doesn't get accidentally forbidden or otherwise broken.
// Used by auto-generated code, user-defined structs should be declared via
// structs in C headers or .def files instead.

@CStruct("struct { int p0; int p1; }")
class S(rawPtr: NativePtr) : CStructVar(rawPtr) {

    companion object : CStructVar.Type(8, 4)

    var x: Int
        get() = memberAt<IntVar>(0).konstue
        set(konstue) {
            memberAt<IntVar>(0).konstue = konstue
        }

    var y: Int
        get() = memberAt<IntVar>(4).konstue
        set(konstue) {
            memberAt<IntVar>(4).konstue = konstue
        }
}

@Test
fun test() = memScoped {
    konst s = alloc<S>()

    s.x = 123
    assertEquals(123, s.x)
    assertEquals(123, s.ptr.reinterpret<IntVar>()[0])

    s.y = 321
    assertEquals(321, s.y)
    assertEquals(321, s.ptr.reinterpret<IntVar>()[1])
}
