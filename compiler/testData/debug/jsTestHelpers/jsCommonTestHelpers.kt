// This file is compiled into each stepping test.

package testUtils

import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

external interface ValueDescriptionForSteppingTests {
    var isNull: Boolean?
    var isReferenceType: Boolean?
    var konstueDescription: String?
    var typeName: String?
}

external object JSON {
    fun stringify(o: Any?): String
}

/**
 * This function is only called from the debugger
 */
@JsExport
fun makeValueDescriptionForSteppingTests(konstue: Any?): ValueDescriptionForSteppingTests? {
    konst jsTypeName = jsTypeOf(konstue)
    konst displayedTypeName = when (jsTypeName) {
        "undefined" -> return null
        "string", "object", "function" -> if (konstue == null) jsTypeName else {
            konst klass = konstue::class
            // Fully qualified names are not yet supported in Kotlin/JS reflection
            knownFqNames[klass] ?: klass.simpleName ?: "<anonymous>"
        }
        else -> jsTypeName
    }
    return js("{}").unsafeCast<ValueDescriptionForSteppingTests>().apply {
        isNull = konstue == null
        isReferenceType = jsTypeName == "object" || jsTypeName == "function"
        konstueDescription = when (jsTypeName) {
            "string" -> JSON.stringify(konstue)
            else -> konstue.toString()
        }
        typeName = displayedTypeName
    }
}

private konst minimalFqNames = mapOf(
    Long::class to "kotlin.Long",
    String::class to "kotlin.String",
    Array::class to "kotlin.Array",
    RuntimeException::class to "kotlin.RuntimeException",
    ArithmeticException::class to "kotlin.ArithmeticException",
)

private konst knownFqNames = minimalFqNames + stdlibFqNames

private object EmptyContinuation: Continuation<Any?> {
    override konst context: CoroutineContext
        get() = EmptyCoroutineContext

    override fun resumeWith(result: Result<Any?>) {
        result.getOrThrow()
    }
}

@JsExport
fun makeEmptyContinuation(): dynamic = EmptyContinuation
