// ISSUE: KT-41989

interface A

interface B {
    fun A.withBottomBorder(): A = this
}

interface C : B {
    konst lineCellStyle: (A.() -> Unit)?
        get() = if (cond()) {
            {
                withBottomBorder()
            }
        } else null
}

fun cond(): Boolean = true
