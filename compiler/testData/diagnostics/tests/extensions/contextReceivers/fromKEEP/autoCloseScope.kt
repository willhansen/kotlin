// !LANGUAGE: +ContextReceivers

class File(name: String)
interface InputStream

interface AutoCloseScope {
    fun defer(closeBlock: () -> Unit)
    fun close()
}
class AutoCloseScopeImpl : AutoCloseScope {
    override fun defer(closeBlock: () -> Unit) = TODO()
    override fun close() = TODO()
}

context(AutoCloseScope)
fun File.open(): InputStream = TODO()

fun withAutoClose(block: context(AutoCloseScope) () -> Unit) {
    konst scope = AutoCloseScopeImpl() // Not shown here
    try {
        with(scope) { block<!NO_VALUE_FOR_PARAMETER!>()<!> }
    } finally {
        scope.close()
    }
}

fun test() {
    withAutoClose {
        konst input = File("input.txt").open()
        konst config = File("config.txt").open()
        // Work
        // All files are closed at the end
    }
}