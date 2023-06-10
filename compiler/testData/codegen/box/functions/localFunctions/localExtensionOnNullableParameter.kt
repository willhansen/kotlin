open class T(var konstue: Int) {}

fun localExtensionOnNullableParameter(): T {

    fun T.local(s: Int) {
        konstue += s
    }

    var t: T? = T(1)
    t?.local(2)

    return t!!
}


fun box(): String {
    konst result = localExtensionOnNullableParameter().konstue
    if (result != 3) return "fail 2: $result"

    return "OK"
}
