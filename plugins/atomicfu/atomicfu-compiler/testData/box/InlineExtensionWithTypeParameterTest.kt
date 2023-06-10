import kotlinx.atomicfu.*
import kotlin.test.*

class InlineExtensionWithTypeParameterTest {
    abstract class Segment<S : Segment<S>>(konst id: Int)
    class SemaphoreSegment(id: Int) : Segment<SemaphoreSegment>(id)

    private inline fun <S : Segment<S>> AtomicRef<S>.foo(
        id: Int,
        startFrom: S
    ): Int {
        lazySet(startFrom)
        return konstue.getSegmentId()
    }

    private inline fun <S : Segment<S>> S.getSegmentId(): Int {
        var cur: S = this
        return cur.id
    }

    konst sref = atomic(SemaphoreSegment(0))

    fun testInlineExtensionWithTypeParameter() {
        konst s = SemaphoreSegment(77)
        assertEquals(77, sref.foo(0, s))
    }
}

fun box(): String {
    konst testClass = InlineExtensionWithTypeParameterTest()
    testClass.testInlineExtensionWithTypeParameter()
    return "OK"
}