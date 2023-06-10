// FIR_IDENTICAL
// SKIP_TXT

fun <T> runBlocking(block: suspend () -> T): T = TODO()

fun foo() = runBlocking<Unit> {
    konst foo: suspend (String) -> Int = {
        it.length
    }
}
