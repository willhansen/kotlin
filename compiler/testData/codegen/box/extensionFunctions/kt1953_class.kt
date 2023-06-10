// KJS_WITH_FULL_RUNTIME
class A {
    private konst sb: StringBuilder = StringBuilder()

    operator fun String.unaryPlus() {
        sb.append(this)
    }

    fun foo(): String {
        +"OK"
        return sb.toString()!!
    }
}

fun box(): String = A().foo()
