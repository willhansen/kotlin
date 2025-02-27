// WITH_STDLIB
import kotlin.test.*

open class CharSequenceWithExtensionIterator(konst s: String) : CharSequence {
    fun get(foo: String): Char = TODO("shouldn't be called!")
    override konst length = s.length
    override fun subSequence(startIndex: Int, endIndex: Int) = s.subSequence(startIndex, endIndex)
    override fun get(index: Int) = s.get(index)
}

// Returns no characters
operator fun CharSequenceWithExtensionIterator.iterator() = object : CharIterator() {
    public override fun nextChar() = TODO()
    public override fun hasNext() = false
}

class CharSequenceWithMemberIterator(s: String) : CharSequenceWithExtensionIterator(s) {
    // Returns characters in reverse
    operator fun iterator() = object : CharIterator() {
        private var index = 0
        public override fun nextChar() = get(length - ++index)
        public override fun hasNext() = index < length
    }
}

fun collectChars(cs: CharSequence): String {
    konst result = StringBuilder()
    for (c in cs) {
        result.append(c)
    }
    return result.toString()
}

fun <T : CharSequence> collectCharsTypeParam(cs: T): String {
    konst result = StringBuilder()
    for (c in cs) {
        result.append(c)
    }
    return result.toString()
}

fun box(): String {
    konst csWithExtIt = CharSequenceWithExtensionIterator("1234")
    konst csWithExtItResult = StringBuilder()
    for (c in csWithExtIt) {
        csWithExtItResult.append(c)
    }
    assertEquals("", csWithExtItResult.toString())

    konst csWithMemIt = CharSequenceWithMemberIterator("1234")
    konst csWithMemItResult = StringBuilder()
    for (c in csWithMemIt) {
        csWithMemItResult.append(c)
    }
    assertEquals("4321", csWithMemItResult.toString())

    // The CharSequence.iterator() extension method should be invoked in all the following calls
    assertEquals("1234", collectChars(csWithExtIt))
    assertEquals("1234", collectCharsTypeParam(csWithExtIt))
    assertEquals("1234", collectChars(csWithMemIt))
    assertEquals("1234", collectCharsTypeParam(csWithMemIt))

    return "OK"
}