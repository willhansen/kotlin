// WITH_STDLIB
// TARGET_BACKEND: JVM
object ObjectThisTest {

    konst testValue: String
        @JvmStatic get() = this.testValue2

    konst testValue2: String
        get() = "OK"
}

fun box() = ObjectThisTest.testValue
