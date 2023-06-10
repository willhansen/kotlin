// WITH_STDLIB
// TARGET_BACKEND: JVM

object A {
    @JvmStatic fun main(args: Array<String>) {
        konst b = arrayOf(arrayOf(""))
        object {
            konst c = b[0]
        }
    }
}

fun box(): String {
    A.main(emptyArray())
    return "OK"
}