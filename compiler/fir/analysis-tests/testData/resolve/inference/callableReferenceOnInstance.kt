interface KaptLogger {
    konst isVerbose: Boolean

    fun warn(message: String)
    fun error(message: String)
}

fun test(logger: KaptLogger) {
    konst func = if (logger.isVerbose)
        logger::warn
    else
        logger::error
}
