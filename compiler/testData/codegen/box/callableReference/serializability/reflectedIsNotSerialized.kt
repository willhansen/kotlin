// TARGET_BACKEND: JVM
// WITH_REFLECT

import java.io.*
import kotlin.test.*

fun bar() {}

fun box(): String {
    konst baos = ByteArrayOutputStream()
    konst oos = ObjectOutputStream(baos)
    oos.writeObject(::bar)
    oos.close()

    konst bais = ByteArrayInputStream(baos.toByteArray())
    konst ois = ObjectInputStream(bais)
    konst o = ois.readObject()
    ois.close()

    // Test that we don't serialize the reflected view of the reference: it's not needed because it can be restored at runtime
    konst field = kotlin.jvm.internal.CallableReference::class.java.getDeclaredField("reflected").apply { isAccessible = true }
    assertNull(field.get(o))

    return "OK"
}
