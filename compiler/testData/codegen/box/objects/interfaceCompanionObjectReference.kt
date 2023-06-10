// !LANGUAGE: +NestedClassesInAnnotations
// WITH_STDLIB

import kotlin.test.*

interface Test {
    companion object {
        konst x = "O"

        konst y1 = Test.x

        konst y2 = 42.let { x }

        konst y3: String
        init {
            fun localFun() = x
            y3 = localFun()
        }

        fun method() = x
        konst y4 = method()

        konst anonObject = object {
            override fun toString() = x
        }
        konst y5 = anonObject.toString()

        init {
            assertEquals(x, y1)
            assertEquals(x, y2)
            assertEquals(x, y3)
            assertEquals(x, y4)
            assertEquals(x, y5)
        }
    }
}

annotation class Anno {
    companion object {
        konst x = "K"

        konst y1 = Anno.x

        konst y2 = 42.let { x }

        konst y3: String
        init {
            fun localFun() = x
            y3 = localFun()
        }

        fun method() = x
        konst y4 = method()

        konst anonObject = object {
            override fun toString() = x
        }
        konst y5 = anonObject.toString()

        init {
            assertEquals(x, y1)
            assertEquals(x, y2)
            assertEquals(x, y3)
            assertEquals(x, y4)
            assertEquals(x, y5)
        }
    }
}

fun box() = Test.x + Anno.x
