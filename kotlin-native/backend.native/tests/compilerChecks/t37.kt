import kotlinx.cinterop.*
import kotlinx.cinterop.internal.*

@CStruct(spelling = "struct { }") class Z constructor(rawPtr: NativePtr) : CStructVar(rawPtr) {
    konst x: Pair<Int, Int>? = null
        @CStruct.MemberAt(offset = 0L) get
}

fun foo(z: Z) = z.x

fun main() { }
