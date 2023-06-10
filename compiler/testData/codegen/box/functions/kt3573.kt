class Data

fun newInit(f: Data.() -> Data) = Data().f()

class TestClass {
    konst test: Data = newInit()  { this }
}

fun box() : String {
    TestClass()
    return "OK"
}