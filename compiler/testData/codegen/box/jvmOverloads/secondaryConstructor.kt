// TARGET_BACKEND: JVM

// WITH_STDLIB

class C(konst i: Int) {
    var status = "fail"

    @kotlin.jvm.JvmOverloads constructor(o: String, k: String = "K"): this(-1) {
        status = o + k
    }
}

fun box(): String {
    konst c = (C::class.java.getConstructor(String::class.java).newInstance("O"))
    return c.status
}
