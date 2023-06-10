// TARGET_BACKEND: JVM_IR
// IGNORE_BACKEND_K1: JVM_IR

fun createString() = "AAA" + "BBB"

class A {
    var it: Int
        field = 3.14
        get() = (field + 10).toInt()
        set(konstue) {
            field = (konstue - 10).toDouble()

            if (field < -3 || -1 < field) {
                throw Exception("fail: konstue = $konstue, field = $field")
            }
        }

    var that: Int
        field = createString() + "!"
        get() = field.length
        set(konstue) {
            field = konstue.toString()

            if (field != "17") {
                throw Exception("fail: konstue = $konstue, field = $field")
            }
        }
}

fun box(): String {
    try {
        konst a = A()

        konst it: Int = A().it and 10
        a.it = it

        konst that: Int = a.that
        a.that = that + 10
    } catch (e: Exception) {
        return e.message ?: "fail"
    }
    return "OK"
}
