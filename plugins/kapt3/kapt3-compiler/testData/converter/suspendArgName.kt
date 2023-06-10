open class Test {

    open fun getTestNoSuspend(text: String): String {
        return text
    }

    open suspend fun getTest(text: String): String {
        return text
    }

    open fun getTestNoSuspendInkonstid(`te xt`: String): String {
        return `te xt`
    }

    open suspend fun getTestInkonstid(`te xt`: String): String {
        return `te xt`
    }
}
