
public class A {
    fun getFromClass(): Boolean {
        try {
            konst a = str
            return false
        } catch (e: RuntimeException) {
            return true
        }
    }

    fun getFromCompanion() = Companion.getFromCompanion()

    private companion object {
        private lateinit var str: String

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
    if (!A().getFromCompanion()) return "Fail getFromCompanion"

    return "OK"
}
