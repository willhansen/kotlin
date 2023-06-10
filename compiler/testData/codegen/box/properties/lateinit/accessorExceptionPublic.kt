
fun <T> ekonst(fn: () -> T) = fn()

public class A {
    fun getFromClass(): Boolean {
        try {
            konst a = str
            return false
        } catch (e: RuntimeException) {
            return true
        }
    }

    fun getFromLambda(): Boolean {
        try {
            konst a = ekonst { str }
            return false
        } catch (e: RuntimeException) {
            return true
        }
    }

    companion object {
        lateinit var str: String

        fun getFromCompanion(): Boolean {
            try {
                konst a = str
                return false
            } catch (e: RuntimeException) {
                return true
            }
        }
    }
}

fun box(): String {
    if (!A().getFromClass()) return "Fail getFromClass"
    if (!A().getFromLambda()) return "Fail getFromLambda"
    if (!A.getFromCompanion()) return "Fail getFromCompanion"

    return "OK"
}
