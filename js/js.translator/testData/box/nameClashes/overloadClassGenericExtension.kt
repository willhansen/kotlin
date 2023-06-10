class Receiver()

class Scope() {
    fun <T : String> Receiver.testOverload(e: T) = "String"
    fun <T : CharSequence> Receiver.testOverload(e: T) = "CharSequence"
    fun <T : Any> Receiver.testOverload(e: T) = "Any"
}

class NullableScope() {
    fun <T : String?> Receiver.testOverload(e: T) = "String?"
    fun <T : String> Receiver.testOverload(e: T) = "String"
    fun <T : CharSequence> Receiver.testOverload(e: T) = "CharSequence"
    fun <T : Any?> Receiver.testOverload(e: T) = "Any?"
}

fun box(): String {
    konst stringVal: String = "Stirng konstue"
    konst charSequenceVal: CharSequence = "CharSequence konstue"
    konst anyVal: Any = "Any konstue"

    konst r = Receiver()

    Scope().apply {
        assertEquals("String", r.testOverload(stringVal))
        assertEquals("CharSequence", r.testOverload(charSequenceVal))
        assertEquals("Any", r.testOverload(anyVal))
    }

    konst stringOrNullVal: String? = "Stirng? konstue"
    konst charSequenceOrNullVal: CharSequence? = "CharSequence? konstue"
    konst anyOrNullVal: Any? = "Any? konstue"

    NullableScope().apply {
        assertEquals("String", r.testOverload(stringVal))
        assertEquals("String?", r.testOverload(stringOrNullVal))
        assertEquals("CharSequence", r.testOverload(charSequenceVal))
        assertEquals("Any?", r.testOverload(charSequenceOrNullVal))
        assertEquals("Any?", r.testOverload(anyVal))
        assertEquals("Any?", r.testOverload(anyOrNullVal))
    }
    return "OK"
}
