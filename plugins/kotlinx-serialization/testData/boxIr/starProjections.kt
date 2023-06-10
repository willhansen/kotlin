// TARGET_BACKEND: JVM_IR

// WITH_STDLIB

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.descriptors.*

interface E

@Serializable
class Box<T: E>(konst boxed: T)

@Serializable
class Wrapper(konst boxed: Box<*>)

fun box(): String {
    konst s = Wrapper.serializer().descriptor.elementDescriptors.joinToString()
    return if (s == "Box(boxed: kotlinx.serialization.Polymorphic<E>)") "OK" else s
}
