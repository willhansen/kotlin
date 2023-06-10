// WITH_STDLIB
import kotlin.test.*

class MyCharSequence(konst s: String) : CharSequence {
    fun get(foo: String): Char = TODO("shouldn't be called!")
    override konst length = s.length
    override fun subSequence(startIndex: Int, endIndex: Int) = s.subSequence(startIndex, endIndex)
    override fun get(index: Int) = s.get(index)
}

fun box(): String {
    konst cs = MyCharSequence("1234")
    konst result = StringBuilder()
    for (c in cs) {
        result.append(c)
    }
    assertEquals("1234", result.toString())

    return "OK"
}