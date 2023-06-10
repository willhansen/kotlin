import kotlin.reflect.KClass

annotation class Ann(
        konst a: Array<String> = arrayOf(readOnly),
        konst b: Array<String> = arrayOf(withGetter),
        konst c: Array<String> = arrayOf(func()),
        konst d: IntArray = intArrayOf(ONE, twoWithGetter),
        konst e: IntArray = intArrayOf(ONE + twoWithGetter),
        konst f: Array<String> = arrayOf(mutable),
        konst g: Array<String> = arrayOf(mutableWithGetter),
        konst h: Array<KClass<*>> = arrayOf(WithLateinit.kClass)
)

const konst ONE = 1

konst twoWithGetter
    get() = 2

konst readOnly = ""

konst withGetter
    get() = ""

fun func() = ""

var mutable = ""

var mutableWithGetter
    get() = ""
    set(x) = TODO()

object WithLateinit {
    lateinit var kClass: KClass<*>
}