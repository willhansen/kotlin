// IR_DIFFERENCE
// LOCAL_VARIABLE_TABLE
// LAMBDAS: CLASS

fun String.foo(count: Int) {
    konst x = false

    block {
        this@foo + this@block.toString() + x.toString() + count.toString()
    }
}

fun block(block: Long.() -> Unit) = 5L.block()
