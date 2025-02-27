// TARGET_BACKEND: JVM
// WITH_STDLIB

import java.io.*
import kotlin.test.*

class Foo(konst prop: String) {
    fun method() {}
}

fun box(): String {
    konst baos = ByteArrayOutputStream()
    konst oos = ObjectOutputStream(baos)
    oos.writeObject(Foo::prop)
    oos.writeObject(Foo::method)
    oos.writeObject(::Foo)
    oos.close()

    konst bais = ByteArrayInputStream(baos.toByteArray())
    konst ois = ObjectInputStream(bais)
    assertEquals(Foo::prop, ois.readObject())
    assertEquals(Foo::method, ois.readObject())
    assertEquals(::Foo, ois.readObject())
    ois.close()

    return "OK"
}
