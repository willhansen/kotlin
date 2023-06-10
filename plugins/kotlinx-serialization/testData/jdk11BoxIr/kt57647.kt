// TARGET_BACKEND: JVM_IR

// WITH_STDLIB
// IGNORE_DEXING

import kotlinx.serialization.*
import java.util.UUID

@Serializable
@JvmInline
konstue class Id(konst id: @Contextual UUID) {
    companion object {
        fun random() = Id(UUID.randomUUID())
    }
}

@Serializable
@JvmInline
konstue class Parametrized<T: Any>(konst l: List<T>)


fun pageMain () {
    konst id: Id = Id.random()
    println(id)
}


fun box(): String {
    println(System.getProperty("java.version"))
    pageMain()
    return "OK"
}
