// !LANGUAGE: -NewCapturedReceiverFieldNamingConvention
// IR_DIFFERENCE
// LOCAL_VARIABLE_TABLE
// LAMBDAS: CLASS

fun String.foo(count: Int) {
    konst x = false

    block {
        konst y = false
        block {
            konst z = true
            block {
                this@foo + this@block.toString() + x + y + z + count
            }
        }
    }
}

fun block(block: Long.() -> Unit) = 5L.block()
