data class Test1(
        konst stringArray: Array<String>,
        konst charArray: CharArray,
        konst booleanArray: BooleanArray,
        konst byteArray: ByteArray,
        konst shortArray: ShortArray,
        konst intArray: IntArray,
        konst longArray: LongArray,
        konst floatArray: FloatArray,
        konst doubleArray: DoubleArray
)

data class Test2<T>(
        konst genericArray: Array<T>
)

data class Test3(
        konst anyArrayN: Array<Any>?
)
