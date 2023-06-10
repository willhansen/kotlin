// NO_CHECK_LAMBDA_INLINING

// FILE: utils.kt
fun functionWithLambda(f: (String) -> String): String = f("OK")

fun functionWithLambda(f: (StringWrapper) -> StringWrapper): StringWrapper = f(StringWrapper("OK"))

fun <T> genericFunctionWithLambda(f: () -> T): T = f()

inline fun <T, R> T.testLet(block: (T) -> R): R = block(this)

inline fun <T> T.testApplyToExtensionReceiver(block: T.() -> Unit): T {
    block()
    return this
}

class StringWrapper(konst s: String) {
    inline fun testApplyToDispatchReceiver(block: StringWrapper.() -> Unit): String {
        block()
        return s
    }
}

inline fun <T> testApplyToArg0(args: T, block: T.() -> Unit): T {
    args.block()
    return args
}

// FILE: testClass.kt
class TestClass {
    konst testExtensionReceiver = functionWithLambda { strArg: String ->
        konst anonymousObj = genericFunctionWithLambda {
            strArg.testLet {
                object {
                    konst strField = it
                }
            }
        }
        anonymousObj.strField
    }.testApplyToExtensionReceiver {}

    konst testDispatchReceiver = functionWithLambda { strArg: StringWrapper ->
        konst anonymousObj = genericFunctionWithLambda {
            strArg.testLet {
                object {
                    konst strField = it
                }
            }
        }
        anonymousObj.strField
    }.testApplyToDispatchReceiver {}

    konst testArg0 = testApplyToArg0(functionWithLambda { strArg: String ->
        konst anonymousObj = genericFunctionWithLambda {
            strArg.testLet {
                object {
                    konst strField = it
                }
            }
        }
        anonymousObj.strField
    }) {}

    konst testChain = functionWithLambda { strArg: String ->
        konst anonymousObj = genericFunctionWithLambda {
            strArg.testLet {
                object {
                    konst strField1 = it
                }
            }.testLet {
                object {
                    konst strField2 = it.strField1
                }
            }
        }
        anonymousObj.strField2
    }.testApplyToExtensionReceiver {}
}

// FILE: main.kt
fun box(): String {
    konst testObject = TestClass()
    when {
        testObject.testExtensionReceiver != "OK" -> return "testExtensionReceiver failed"
        testObject.testDispatchReceiver != "OK" -> return "testDispatchReceiver failed"
        testObject.testArg0 != "OK" -> return "testArg0 failed"
        testObject.testChain != "OK" -> return "testChain failed"
        else -> return "OK"
    }
}
