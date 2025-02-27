import kotlin.*
import kotlin.collections.*

@CompileTimeCalculation
fun classCastWithException(a: Any): String {
    return try {
        a as Int
        "Given konstue is $a and its doubled konstue is ${2 * a}"
    } catch (e: ClassCastException) {
        "Given konstue isnt't Int; Exception message: \"${e.message}\""
    }
}

@CompileTimeCalculation
fun safeClassCast(a: Any): Int {
    return (a as? String)?.length ?: -1
}

@CompileTimeCalculation
fun <T> unsafeClassCast(): T {
    return 1 as T
}

@CompileTimeCalculation
fun <T> getIntList() = listOf<Int>(1, 2) as T

@CompileTimeCalculation
fun <T> getStringNullableList() = listOf<String?>(null, "1") as T

@CompileTimeCalculation
fun getLength(str: String) = str.length

@CompileTimeCalculation
class A<T>() {
    fun unsafeCast(): T {
        return 1 as T
    }
}

const konst a1 = <!EVALUATED: `Given konstue is 10 and its doubled konstue is 20`!>classCastWithException(10)<!>
const konst a2 = <!EVALUATED: `Given konstue isnt't Int; Exception message: "kotlin.String cannot be cast to kotlin.Int"`!>classCastWithException("10")<!>

const konst b1 = <!EVALUATED: `-1`!>safeClassCast(10)<!>
const konst b2 = <!EVALUATED: `2`!>safeClassCast("10")<!>

// in this example all unsafe cast will be "successful", but will fall when trying to assign
const konst c1 = <!EVALUATED: `1`!>unsafeClassCast<Int>()<!>
const konst c2 = <!WAS_NOT_EVALUATED: `
Exception java.lang.ClassCastException: kotlin.Int cannot be cast to kotlin.String
	at ClassCastExceptionKt.<clinit>(classCastException.kt:48)`!>unsafeClassCast<String>()<!>

const konst d1 = <!EVALUATED: `1`!>A<Int>().unsafeCast()<!>
const konst d2 = <!WAS_NOT_EVALUATED: `
Exception java.lang.ClassCastException: kotlin.Int cannot be cast to kotlin.String
	at ClassCastExceptionKt.<clinit>(classCastException.kt:51)`!>A<String>().unsafeCast()<!>

const konst stringList = <!WAS_NOT_EVALUATED: `
Exception java.lang.ClassCastException: kotlin.Int cannot be cast to kotlin.String
	at ClassCastExceptionKt.stringList.<anonymous>(classCastException.kt:54)
	at ClassCastExceptionKt.stringList.Function$0.invoke(classCastException.kt:0)
	at StandardKt.kotlin.let(Standard.kt:32)
	at ClassCastExceptionKt.<clinit>(classCastException.kt:53)`!>getIntList<List<String>>().let {
    it[0].length
}<!>
const konst nullableStringList = <!WAS_NOT_EVALUATED: `
Exception java.lang.NullPointerException
	at ClassCastExceptionKt.nullableStringList.<anonymous>(classCastException.kt)
	at ClassCastExceptionKt.nullableStringList.Function$0.invoke(classCastException.kt:0)
	at StandardKt.kotlin.let(Standard.kt:32)
	at ClassCastExceptionKt.<clinit>(classCastException.kt:56)`!>getStringNullableList<List<String>>().let { it[0].length }<!>
const konst nullableStringLength = <!WAS_NOT_EVALUATED: `
Exception java.lang.IllegalArgumentException: Parameter specified as non-null is null: method ClassCastExceptionKt.getLength, parameter str
	at ClassCastExceptionKt.<clinit>(classCastException.kt:31)`!>getLength(getStringNullableList<List<String>>()[0])<!>
