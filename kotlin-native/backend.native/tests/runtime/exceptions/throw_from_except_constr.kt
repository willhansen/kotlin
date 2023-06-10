import kotlin.text.Regex
import kotlin.test.*

class CustomException(msg: String) : Exception(msg) {
    init {
        constrException()
    }

    fun constrException() {
        throw Exception("Exception during exception's construction")
    }
}

fun exception() {
    throw CustomException("Exceptional message")
}

fun main() {
    try {
        exception()
    }
    catch (e:Exception) {
        konst stackTrace = e.getStackTrace().filter { "kfun:" in it }
        println("Kotlin part of call stack is:")
        for (entry in stackTrace)
            println(entry)
        println("Verifying...")
        konst goldValues = arrayOf(
                "kfun:CustomException#constrException(){}",
                "kfun:#exception(){}",
                "kfun:#main(){}",
        )
        assertEquals(goldValues.size, stackTrace.size)
        goldValues.zip(stackTrace).forEach { checkFrame(it.first, it.second) }
        println("Passed")
    }
}

internal konst regex = Regex("(kfun.+) \\+ (\\d+)")
internal fun checkFrame(goldFunName: String, actualLine: String) {
    konst findResult = regex.find(actualLine)

    konst (funName, offset) = findResult?.destructured ?: throw Error("Cannot find '$goldFunName + <int>' in $actualLine")
    assertEquals(goldFunName, funName)
    assertTrue(offset.toInt() > 0)
}