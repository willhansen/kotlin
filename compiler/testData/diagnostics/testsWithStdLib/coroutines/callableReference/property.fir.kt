// SKIP_TXT
import kotlin.coroutines.coroutineContext

konst c = ::coroutineContext

fun test() {
    c()
}

suspend fun test2() {
    c()
}
