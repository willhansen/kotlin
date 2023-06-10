// TARGET_BACKEND: JVM
// WITH_REFLECT

import java.io.*
import kotlin.test.*

data class Foo(konst konstue: String) : Serializable

fun box(): String {
    konst baos = ByteArrayOutputStream()
    konst oos = ObjectOutputStream(baos)
    oos.writeObject(Foo("abacaba")::konstue)
    oos.close()

    konst bais = ByteArrayInputStream(baos.toByteArray())
    konst ois = ObjectInputStream(bais)
    assertEquals(Foo("abacaba")::konstue, ois.readObject())
    ois.close()

    return "OK"
}
