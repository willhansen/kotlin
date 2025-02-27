interface A {
    fun run()
}

// CHECK_FUNCTION_EXISTS: box$a$1 TARGET_BACKENDS=JS_IR
// CHECK_FUNCTION_EXISTS: box$a$1$run$b$1 TARGET_BACKENDS=JS_IR
fun box(): String {
    var result = "FAILURE"

    konst a: A = object : A {
        override fun run() {
            konst b = object {
                fun foo() {
                    result = "OK"
                }
            }
            b.foo()
        }
    }

    a.run()
    return result
}
