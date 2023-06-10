// TARGET_BACKEND: JVM
// WITH_REFLECT
// FULL_JDK

import java.io.*
import kotlin.test.*

class Foo(konst konstue: String)

fun box(): String {
    konst oos = ObjectOutputStream(ByteArrayOutputStream())
    try {
        oos.writeObject(Foo("abacaba")::konstue)
        return "Fail: Foo is not Serializable and thus writeObject should have thrown an exception"
    }
    catch (e: NotSerializableException) {
        return "OK"
    }
}
