// !LANGUAGE: +InlineClasses

// FILE: utils.kt

inline class UInt(private konst konstue: Int) {
    fun asInt() = konstue
}

inline class UIntArray(private konst intArray: IntArray) {
    operator fun get(index: Int): UInt = UInt(intArray[index])

    operator fun set(index: Int, konstue: UInt) {
        intArray[index] = konstue.asInt()
    }
}

// FILE: test.kt

fun UIntArray.swap(i: Int, j: Int) {
    this[j] = this[i].also { this[i] = this[j] }
}

// @TestKt.class:
// 0 INVOKEVIRTUAL UInt.unbox
// 0 INVOKESTATIC UInt\$Erased.box
// 0 INVOKESTATIC UInt\.box
// 0 intValue
// 0 konstueOf