// WITH_REFLECT

import kotlin.reflect.*

annotation class Ann(vararg konst allowedTypes: KClass<*>)

fun foo() {
    class Local {
        @field:Ann(allowedTypes = [Some::class, Other::class])
        konst x: Int = 42
    }
}

class Some
class Other
