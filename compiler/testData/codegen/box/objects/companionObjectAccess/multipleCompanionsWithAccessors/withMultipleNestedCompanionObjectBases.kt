// !LANGUAGE: +ProperVisibilityForCompanionObjectInstanceField
// FILE: withCompanionObjectBase.kt
import b.*

fun box() = B.vok

// FILE: a.kt
package a

open class A1 {
    protected companion object {
        fun getO() = "O"
    }
}

open class A2 {
    protected companion object {
        fun getK() = "K"
    }
}

// FILE: b.kt
package b

import a.*

class B {
    class B1 {
        companion object : A1() {
            konst vo = getO()
        }

        class B2 {
            companion object : A2() {
                konst vk = getK()
            }
        }
    }

    companion object {
        konst vok = B1.vo + B1.B2.vk
    }
}
