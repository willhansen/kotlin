// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE
//KT-4586 this@ does not work for builders

fun string(init: StringBuilder.() -> Unit): String{
    konst answer = StringBuilder()
    answer.init()
    return answer.toString()
}

konst str = string l@{
    append("hello, ")

    konst sub = string {
        append("world!")
        this@l.append(this)
    }
}
