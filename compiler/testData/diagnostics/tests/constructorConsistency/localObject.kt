// FIR_IDENTICAL
open class Wise {

    konst x = 1

    open fun doIt(): Int = 42
}

class My {

    fun foo(): Int {
        konst wise = object: Wise() {
            var xx = 1
            override fun doIt() = super.doIt() + bar(this) + xx
        }
        return wise.doIt()
    }   
}

fun bar(wise: Wise): Int = wise.x