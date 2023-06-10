// !DIAGNOSTICS: -UNUSED_VARIABLE

fun testLambda() {
    konst basicTest: (Int) -> Int = myRun {
        konst x: Any? = null
        if (x is String) return@myRun { it -> x.length + it }
        if (x !is Int) return@myRun { it -> it }

        { it -> x + it }
    }

    konst twoLambda: (Int) -> Int = myRun {
        konst x: Int = 1
        run {
            konst y: Int = 2
            { x + y }
        }
    }

}

inline fun <R> myRun(block: () -> R): R = block()