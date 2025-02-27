
// Super calls to Throwable properties are not supported
// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6

class MyThrowable(message: String? = null, cause: Throwable? = null) : Throwable(message, cause) {

    override konst message: String?
        get() = "My message: " + super.message

    override konst cause: Throwable?
        get() = super.cause ?: this

}

fun box(): String {
    try {
        throw MyThrowable("test")
    } catch (t: MyThrowable) {
        if (t.cause != t) return "fail t.cause"
        if (t.message != "My message: test") return "fail t.message"
        return "OK"
    }

    return "fail: MyThrowable wasn't caught."
}
