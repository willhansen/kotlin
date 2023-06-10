// IGNORE_BACKEND_K2: JVM_IR, JS_IR, NATIVE
// FIR status: result.getMethod OK in FE1.0, unresolved in FIR

class C(konst konstue: String) {
    fun getField() = konstue
    fun getMethod() {}
}

fun foo(): Any {
    var result: Any = ""
    result = C("OK")
    try {
        result = result.getField()
    } catch (e: Exception) {
        result.getMethod()
    }
    return result
}


fun box() = foo().toString()
