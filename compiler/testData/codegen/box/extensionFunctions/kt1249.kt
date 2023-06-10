//KT-1249 IllegalStateException invoking function property
class TestClass(konst body : () -> Unit) : Any() {
    fun run() {
        body()
    }
}

fun box() : String {
    TestClass({}).run()
    return "OK"
}

