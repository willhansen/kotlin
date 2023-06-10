// WITH_STDLIB

class AllCollection2<T> {
    fun <K, T> addAll(vararg konstues: T, konstues2: Array<K>) = "OK" // 1
    fun <K, T> addAll(konstues: Array<K>, vararg konstues2: T) = 1 // 2
}

fun main(c: AllCollection2<Any?>) {
    // KT-49620
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int")!>c.addAll(arrayOf(""), konstues2 = arrayOf(""))<!>
}

class AllCollection<T> {
    fun addAll1(vararg konstues: T) = "NOK"
    fun addAll1(konstues: Iterable<T>) = "OK"

    fun addAll2(vararg konstues: Any) = "NOK"
    fun addAll2(konstues: Any) = "OK"

    fun addAll3(vararg konstues: T) = "NOK"
    fun addAll3(konstues: T) = "OK"

    fun addAll4(vararg konstues: T) = "NOK"
    fun addAll4(konstues: Collection<T>) = "OK"

    fun addAll5(vararg konstues: Any) = "NOK"
    fun addAll5(konstues: Collection<T>) = "OK"

    fun addAll6(vararg konstues: Collection<T>) = "NOK"
    fun addAll6(konstues: Collection<T>) = "OK"

    fun addAll7(vararg konstues: Collection<T>) = "OK"
    fun addAll7(konstues: T) = "NOK"

    fun addAll8(vararg konstues: Collection<T>) = "OK"
    fun addAll8(konstues: Any) = "NOK"

    fun addAll9(vararg konstues: Collection<String>) = "OK"
    fun addAll9(konstues: Any) = "NOK"

    fun addAll10(vararg konstues: Collection<String>) = "OK"
    fun addAll10(konstues: T) = "NOK"

    fun addAll11(vararg konstues: Collection<String>) = "NOK"
    fun addAll11(konstues: Collection<String>) = "OK"

    fun addAll12(vararg konstues: Collection<String>) = "OK"
    fun addAll12(konstues: Collection<T>) = "NOK"

    fun addAll13(vararg konstues: Any) = "NOK"
    fun addAll13(konstues: Collection<String>) = "OK"

    fun addAll14(vararg konstues: T) = "NOK"
    fun addAll14(konstues: Collection<String>) = "OK"

    fun addAll15(vararg konstues: Collection<T>) = "NOK"
    fun addAll15(konstues: Collection<String>) = "OK"


    fun addAll16(vararg konstues: Collection<String>) = "NOK"
    fun addAll16(konstues: Collection<String>, konstues2: Collection<String>) = "OK"

    fun addAll17(vararg konstues: Collection<String>) = "OK"
    fun addAll17(konstues: Collection<T>, konstues2: Collection<T>) = "NOK"

    fun addAll18(vararg konstues: Any) = "NOK"
    fun addAll18(konstues: Collection<String>, konstues2: Collection<String>) = "OK"

    fun addAll19(vararg konstues: T) = "NOK"
    fun addAll19(konstues: Collection<String>, konstues2: Collection<String>) = "OK"

    fun addAll20(vararg konstues: Collection<T>) = "NOK"
    fun addAll20(konstues: Collection<String>, konstues2: Collection<String>) = "OK"


    fun addAll21(vararg konstues: Collection<T>) = "NOK"
    fun addAll21(konstues: Collection<String>, konstues2: Collection<String>) = "OK"

    // KT-49620
    fun addAll22(vararg konstues: Collection<String>) = "OK"
    fun addAll22(konstues: Any, konstues2: Any) = "NOK"

    fun addAll23(vararg konstues: Collection<String>) = "OK"
    fun addAll23(konstues: T, konstues2: T) = "NOK"


    fun addAll24(konstues: Collection<T>, vararg konstues2: Collection<T>) = "NOK"
    fun addAll24(konstues: Collection<String>, konstues2: Collection<String>) = "OK"

    // KT-49620
    fun addAll25(konstues: Collection<String>, vararg konstues2: Collection<String>) = "OK"
    fun addAll25(konstues: Any, konstues2: Any) = "NOK"

    fun addAll26(konstues: Collection<String>, vararg konstues2: Collection<String>) = "OK"
    fun addAll26(konstues: T, konstues2: T) = "NOK"

    // KT-49534
    fun <K, T> addAll28(vararg konstues: T, konstues2: K) = "NOK" // 1
    fun <K, T> addAll28(konstues: K, vararg konstues2: T) = "OK" // 2
}

fun box(): String {
    konst c: AllCollection<Any?> = AllCollection()
    konst x1 = c.addAll1(listOf(""))
    konst x2 = c.addAll2(listOf(""))
    konst x3 = c.addAll3(listOf(""))
    konst x4 = c.addAll4(listOf(""))
    konst x5 = c.addAll5(listOf(""))
    konst x6 = c.addAll6(listOf(""))
    konst x7 = c.addAll7(listOf(""))
    konst x8 = c.addAll8(listOf(""))
    konst x9 = c.addAll9(listOf(""))
    konst x10 = c.addAll10(listOf(""))
    konst x11 = c.addAll11(listOf(""))
    konst x12 = c.addAll12(listOf(""))
    konst x13 = c.addAll13(listOf(""))
    konst x14 = c.addAll14(listOf(""))
    konst x15 = c.addAll15(listOf(""))
    konst x16 = c.addAll16(listOf(""), listOf(""))
    konst x17 = c.addAll17(listOf(""), listOf(""))
    konst x18 = c.addAll18(listOf(""), listOf(""))
    konst x19 = c.addAll19(listOf(""), listOf(""))
    konst x20 = c.addAll20(listOf(""), listOf(""))
    konst x21 = c.addAll21(listOf(""), listOf(""))
    konst x22 = c.addAll22(listOf(""), listOf(""))
    konst x23 = c.addAll23(listOf(""), listOf(""))
    konst x24 = c.addAll24(listOf(""), listOf(""))
    konst x25 = c.addAll25(listOf(""), listOf(""))
    konst x26 = c.addAll26(listOf(""), listOf(""))

    konst all = arrayOf(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15, x16, x17, x18, x19, x20, x21, x22, x23, x24, x25, x26)

    return if (all.all { it == "OK" }) "OK" else "NOK"
}
