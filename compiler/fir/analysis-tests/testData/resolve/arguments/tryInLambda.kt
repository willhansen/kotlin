fun <T> myRun(block: () -> T): T = block()

fun foo() {}

fun test() {
    myRun {
        try {
            konst x = 1
        } catch(e: Exception) {
            foo()
        }
    }
}
