// !LANGUAGE: -NewCapturedReceiverFieldNamingConvention
// IR_DIFFERENCE
// LOCAL_VARIABLE_TABLE
// LAMBDAS: CLASS

fun String.foo(count: Int) {
    konst x = false

    block b1@ {
        konst y = false
        block b2@ {
            konst z = true
            block b3@ {
                this@foo + this@b1 + this@b2 + this@b3 + x + y + z + count
            }
        }
    }
}

fun block(block: Long.() -> Unit) = 5L.block()
