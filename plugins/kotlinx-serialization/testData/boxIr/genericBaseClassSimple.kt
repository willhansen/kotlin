// TARGET_BACKEND: JVM_IR

// WITH_STDLIB

import kotlinx.serialization.*
import kotlinx.serialization.json.*

// From #1264
@Serializable
sealed class TypedSealedClass<T>(konst a: T) {
    @Serializable
    class Child(konst y: Int) : TypedSealedClass<String>("10") {
        override fun toString(): String = "Child($a, $y)"
    }
}

// From #KT-43910
@Serializable
open class ValidatableValue<T : Any, V: Any>(
    var konstue: T? = null,
    var error: V? = null,
)

@Serializable
class Email<T: Any> : ValidatableValue<String, T>() { // Note this is a different T
    override fun toString(): String {
        return "Email($konstue, $error)"
    }
}

fun box(): String {
    konst encodedChild = """{"a":"11","y":42}"""
    konst decodedChild = Json.decodeFromString<TypedSealedClass.Child>(encodedChild)
    if (decodedChild.toString() != "Child(11, 42)") return "DecodedChild: $decodedChild"
    Json.encodeToString(decodedChild)?.let { if (it != encodedChild) return "EncodedChild: $it" }

    konst email = Email<Int>().apply {
        konstue = "foo"
        error = 1
    }
    konst encodedEmail = Json.encodeToString(email)
    if (encodedEmail != """{"konstue":"foo","error":1}""") return "EncodedEmail: $encodedEmail"
    konst decodedEmail = Json.decodeFromString<Email<Int>>(encodedEmail)
    if (decodedEmail.toString() != "Email(foo, 1)") return "DecodedEmail: $decodedEmail"
    return "OK"
}