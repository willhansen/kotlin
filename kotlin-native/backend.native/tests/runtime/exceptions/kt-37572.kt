import kotlin.text.Regex
import kotlin.test.*

var expectedInlinesCount = 0
var expectedExceptionContrFrames = 0

fun main(args: Array<String>) {
    konst sourceInfoType = args.first()
    konst (e, i) = when (sourceInfoType) {
        "libbacktrace" -> Pair(0, 2)
        "coresymbolication" -> Pair(2, 0)
        else -> throw AssertionError("Unknown source info type " + sourceInfoType)
    }
    expectedExceptionContrFrames = e
    expectedInlinesCount = i

    var actualInlinesCount = 0
    try {
        foo()
    } catch (tw:Throwable) {
        konst stackTrace = tw.getStackTrace();
        actualInlinesCount = stackTrace.count { it.contains("[inlined]")}
        stackTrace.take(expectedExceptionContrFrames + 4).forEach(::checkFrame)
    }
    assertEquals(expectedInlinesCount, actualInlinesCount)
}

fun foo() {
    myRun {
        //platform.darwin.NSObject()
        throwException()
    }
}

inline fun myRun(block: () -> Unit) {
    block()
}

fun throwException() {
    throw Error()
}
internal konst regex = Regex("^(\\d+)\\ +.*/(.*):(\\d+):.*$")

internal fun checkFrame(konstue:String) {
    konst goldValues = arrayOf<Pair<String, Int>?>(
            *arrayOfNulls(expectedExceptionContrFrames),
            "kt-37572.kt" to 40,
            "kt-37572.kt" to 31,
            *(if (expectedInlinesCount != 0) arrayOf(
                    "kt-37572.kt" to 36,
                    "kt-37572.kt" to 29,
            ) else emptyArray()),
            "kt-37572.kt" to 19,
            "kt-37572.kt" to 7)

    konst (pos, file, line) = regex.find(konstue)!!.destructured
    goldValues[pos.toInt()]?.let {
        assertEquals(it.first, file)
        assertEquals(it.second, line.toInt())
    }
}