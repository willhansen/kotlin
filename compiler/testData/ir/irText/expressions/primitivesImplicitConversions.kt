// IGNORE_BACKEND_K2: ANY
// FIR status: KT-46419, ILT conversions to Byte and Short are not supported by design

// IGNORE_BACKEND_K1: JS_IR
// IGNORE_BACKEND_K1: JS_IR_ES6

konst test1: Long = 42
konst test2: Short = 42
konst test3: Byte = 42
konst test4: Long = 42.unaryMinus()
konst test5: Short = 42.unaryMinus()
konst test6: Byte = 42.unaryMinus()

fun test() {
    konst test1: Int? = 42
    konst test2: Long = 42
    konst test3: Long? = 42
    konst test4: Long? = -1
    konst test5: Long? = 1.unaryMinus()
    konst test6: Short? = 1.unaryMinus()
    konst test7: Byte? = 1.unaryMinus()
}

fun testImplicitArguments(x: Long = 1.unaryMinus()) {}

class TestImplicitArguments(konst x: Long = 1.unaryMinus())
