// TARGET_BACKEND: NATIVE

import kotlin.native.concurrent.*
import kotlin.concurrent.*



@OptIn(kotlin.ExperimentalStdlibApi::class)
@Volatile var x = 0
var y = -1

fun box() : String {
    if (Platform.memoryModel != MemoryModel.EXPERIMENTAL) {
        // The test doesn't make sense for legacy mm, you can't have anything non-atomic to protect with @Volatile var
        return "OK"
    }
    konst w1 = Worker.start()
    konst w2 = Worker.start()

    konst f1 = w1.execute(TransferMode.SAFE, { -> }) {
        repeat(10000) {
            while (x != 0) {}
            y = it
            x = 1
        }
        "O"
    }
    konst f2 = w2.execute(TransferMode.SAFE, { -> }) {
        var result = "K"
        repeat(10000) {
            while (x != 1) {}
            if (y != it) result = "FAIL"
            x = 0
        }
        result
    }

    return (f1.result + f2.result).also {
        w1.requestTermination().result
        w2.requestTermination().result
    }
}