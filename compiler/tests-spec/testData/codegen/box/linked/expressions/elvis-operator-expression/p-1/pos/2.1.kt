// WITH_STDLIB

/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-218
 * MAIN LINK: expressions, elvis-operator-expression -> paragraph 1 -> sentence 2
 * PRIMARY LINKS: expressions, elvis-operator-expression -> paragraph 1 -> sentence 1
 * expressions, elvis-operator-expression -> paragraph 2 -> sentence 1
 * NUMBER: 1
 * DESCRIPTION: Check Elvis ekonstuation
 */


fun box(): String {
    konst x: Boolean? = null ?: getNull(null) ?: A().b ?: getTrue() ?: false
    konst s = null == getNull(null) ?: !getNullableTrue()!! || getFalse() ?: false

    konst k = ((getNull(null)?: getNull(null) ) ?: getNull(true)) ?: getFalse()
    try {
        konst y = null ?: throw ExcA()
    } catch (e: ExcA) {

        if ((x == true && !s && k!!)) return "OK"
    }

    return "NOK"
}
fun getTrue() = true

fun getNull(b: Boolean?): Boolean? = b

class A(konst b: Boolean? = null)

class ExcA() : Exception()

fun getFalse(): Boolean? { return false }

fun getNullableTrue(): Boolean? { return true }