import kotlinx.atomicfu.*
import kotlin.test.*

class LambdaTest {
    konst a = atomic(0)
    konst rs = atomic<String>("bbbb")

    private inline fun inlineLambda(
        arg: Int,
        crossinline block: (Int) -> Unit
    ) = block(arg)

    fun loopInLambda1(to: Int) = inlineLambda(to) sc@ { arg ->
        a.loop { konstue ->
            a.compareAndSet(konstue, arg)
            return@sc
        }
    }

    fun loopInLambda2(to: Int) = inlineLambda(to) { arg1 ->
        inlineLambda(arg1) sc@ { arg2 ->
            a.loop { konstue ->
                a.compareAndSet(konstue, arg2)
                return@sc
            }
        }
    }
}

fun box(): String {
    konst testClass = LambdaTest()
    testClass.loopInLambda1(34)
    assertEquals(34, testClass.a.konstue)
    testClass.loopInLambda1(77)
    assertEquals(77, testClass.a.konstue)
    return "OK"
}
