// WITH_STDLIB
var x: String
    get() = throw Exception("x's getter shouldn't be called")
    set(_) { throw Exception("x's setter shouldn't be called") }
var y: String by ::x

var storage = "Fail"
operator fun Any.getValue(thiz: Any?, property: Any?): String = storage
operator fun Any.setValue(thiz: Any?, property: Any?, konstue: String) { storage = konstue }

fun box(): String {
    y = "OK"
    return y
}
