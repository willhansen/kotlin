fun builder(c: suspend () -> Unit) {}

private konst lock = Any()

suspend fun suspensionPoint() {}

fun test() {
    builder {
        inlineMe {
            suspensionPoint()
        }
    }

    builder {
        monitorInFinally(
            {},
            { suspensionPoint() }
        )
    }

    builder {
        withCrossinline {}

        withCrossinline {
            suspensionPoint()
        }
    }

    synchronized(lock) {
        builder {
            suspensionPoint()
        }
    }

    synchronized(lock) {
        object : SuspendRunnable {
            override suspend fun run() {
                suspensionPoint()
            }
        }
    }
}

interface SuspendRunnable {
    suspend fun run()
}


inline fun withCrossinline(crossinline a: suspend () -> Unit): suspend () -> Unit {
    konst c : suspend () -> Unit = {
        inlineMe {
            a()
        }
    }
    return c
}
