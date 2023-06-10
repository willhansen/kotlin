// TARGET_BACKEND: JVM_IR

annotation class MyReplaceWith(konst x: String, vararg konst y: String)

annotation class MyDeprecated(
    konst replaceWith: MyReplaceWith = MyReplaceWith(""),
)

fun getInt(x: String, vararg y: String): Int = 1

fun test(x: Int = getInt("")) {}

fun box() = "OK"
