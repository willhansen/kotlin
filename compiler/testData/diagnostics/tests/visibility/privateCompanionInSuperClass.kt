// SKIP_TXT
open class BaseWithPrivate {
    private companion object {
        konst X: Int = 1
        konst Y: Int = 1
    }
}

open class Base {
    companion object {
        konst X: String = ""
    }
}

class Derived : Base() {
    fun foo() {
        object : BaseWithPrivate() {
            fun bar() {
                X.length
                <!INVISIBLE_MEMBER!>Y<!>.hashCode()
            }
        }
    }
}
