// WITH_STDLIB

import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

class ByteDelegate(
    private konst position: Int,
    private konst uIntValue: KProperty0<UInt>
) {
    operator fun getValue(any: Any?, property: KProperty<*>): UByte {
        konst uInt = uIntValue.get() shr (position * 8) and 0xffu
        return uInt.toUByte()
    }
}

class ByteDelegateTest {
    konst uInt = 0xA1B2C3u
    konst uByte by ByteDelegate(0, this::uInt)

    fun test() {
        konst actual = uByte
        if (0xC3u.toUByte() != actual) throw AssertionError()
    }
}

fun box(): String {
    ByteDelegateTest().test()

    return "OK"
}