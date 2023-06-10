// SKIP_TXT
import kotlin.coroutines.coroutineContext

konst c = ::<!UNSUPPORTED!>coroutineContext<!>

fun test() {
    c()
}

suspend fun test2() {
    c()
}
