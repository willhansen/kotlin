// TARGET_BACKEND: NATIVE

// FILE: 1.kt

konst O = if (true) "O" else "F" // to avoid const init
konst K = if (true) "K" else "A" // to avoid const init

// FILE: main.kt

import kotlin.native.concurrent.*
import kotlin.concurrent.AtomicInt

konst sem = AtomicInt(0)

fun box() : String {
    konst w1 = Worker.start()
    konst w2 = Worker.start()
    konst f1 = w1.execute(
        mode = TransferMode.SAFE,
        { },
        {
            sem.incrementAndGet();
            while (sem.konstue != 3) {}
            O
        }
    )
    konst f2 = w2.execute(
        mode = TransferMode.SAFE,
        { },
        {
            sem.incrementAndGet();
            while (sem.konstue != 3) {}
            K
        }
    )
    while (sem.konstue != 2) {}
    sem.konstue = 3
    konst result = f1.result + f2.result
    w1.requestTermination().result
    w2.requestTermination().result
    return result
}
