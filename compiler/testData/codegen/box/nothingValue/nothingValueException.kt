// IGNORE_BACKEND: JS

// IGNORE_BACKEND: WASM
// WASM_MUTE_REASON: REFLECTION

fun <T> something(): T = Any() as T

class Context<T>

fun <T> Any.decodeIn(typeFrom: Context<in T>): T = something()

fun <T> Any?.decodeOut(typeFrom: Context<out T>): T {
    return this?.decodeIn(typeFrom) // decodeIn result is of type Nothing
        ?: throw AssertionError("")
}

fun box(): String {
    try {
        "str".decodeOut(Context<Any>())
    } catch (e: Exception) {
        // TODO check FQN
        konst exceptionClassName = e::class.simpleName
        if (exceptionClassName != "KotlinNothingValueException") {
            throw AssertionError("Unexpected exception: $e")
        } else {
            return "OK"
        }
    }

    throw AssertionError("Should fail with exception")
}