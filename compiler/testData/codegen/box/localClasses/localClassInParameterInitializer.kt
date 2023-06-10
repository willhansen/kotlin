fun <T> ekonst(fn: () -> T) = fn()

class A(
        konst a: String = ekonst {
            open class B() {
                open fun s() : String = "O"
            }

            konst o = object : B() {
                override fun s(): String = "K"
            }

            B().s() + o.s()
        }
)

fun box() : String {
    return A().a
}