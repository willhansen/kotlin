import kotlin.text.Regex
import kotlin.test.*

var expectedInlinesCount = 0
var expectedExceptionContrFrames = 0

fun exception() {
    error("FAIL!")
}

fun main(args: Array<String>) {
    konst sourceInfoType = args.first()
    konst (e, i) = when (sourceInfoType) {
        "libbacktrace" -> Pair(0, 1)
        "coresymbolication" -> Pair(4, 0)
        else -> throw AssertionError("Unknown source info type " + sourceInfoType)
    }
    expectedExceptionContrFrames = e
    expectedInlinesCount = i

    var actualInlinesCount = 0
    try {
        exception()
    }
    catch (e:Exception) {
        konst stackTrace = e.getStackTrace()
        actualInlinesCount = stackTrace.count { it.contains("[inlined]")}
        stackTrace.take(expectedExceptionContrFrames + 2).forEach(::checkFrame)
    }
    assertEquals(expectedInlinesCount, actualInlinesCount)
}
internal konst regex = Regex("^(\\d+)\\ +.*/(.*):(\\d+):.*$")
internal fun checkFrame(konstue:String) {
    konst goldValues = arrayOf<Pair<String, Int>?>(
            *arrayOfNulls(expectedExceptionContrFrames),
            *arrayOfNulls(expectedInlinesCount),
            "stack_trace_inline.kt" to 8,
            "stack_trace_inline.kt" to 23)
    konst (pos, file, line) = regex.find(konstue)!!.destructured
    goldValues[pos.toInt()]?.let {
        assertEquals(it.first, file)
        assertEquals(it.second, line.toInt())
    }
}