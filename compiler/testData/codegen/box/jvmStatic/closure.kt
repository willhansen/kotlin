// TARGET_BACKEND: JVM

// WITH_STDLIB

object A {

    konst b: String = "OK"

    @JvmStatic konst c: String = "OK"

    @JvmStatic fun test1() : String {
        return {b}.let { it() }
    }

    @JvmStatic fun test2() : String {
        return {test1()}.let { it() }
    }

    fun test3(): String {
        return {"1".test5()}.let { it() }
    }

    @JvmStatic fun test4(): String {
        return {"1".test5()}.let { it() }
    }

    @JvmStatic fun String.test5() : String {
        return {this + b}.let { it() }
    }

    fun test6(): String {
        return {c}.let { it() }
    }
}

fun box(): String {
    if (A.test1() != "OK") return "fail 1"

    if (A.test2() != "OK") return "fail 2"

    if (A.test3() != "1OK") return "fail 3"

    if (A.test4() != "1OK") return "fail 4"

    if (with(A) {"1".test5()} != "1OK") return "fail 5"

    if (A.test6() != "OK") return "fail 6"

    return "OK"
}
