// FIR_IDENTICAL
// !LANGUAGE: +ContextReceivers

interface Params
interface Logger {
    fun info(message: String)
}
interface LoggingContext {
    konst log: Logger // this context provides reference to logger
}

context(LoggingContext)
fun performSomeBusinessOperation(withParams: Params) {
    log.info("Operation has started")
}