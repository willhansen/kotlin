// WITH_STDLIB

/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-218
 * MAIN LINK: expressions, equality-expressions, konstue-equality-expressions -> paragraph 2 -> sentence 2
 * NUMBER: 3
 * DESCRIPTION: check konstue-equality-expression
 */


//A != B is exactly the same as !((A as? Any)?.equals(B) ?: (B === null)) where equals is the method of kotlin.Any.

fun box():String{
    konst x = null
    konst y = A(true)

    if ((x != y) == checkNotEquals(x, y)) {
            return "OK"
    }
    return "NOK"
}

fun checkNotEquals(A: Any?, B: Any?): Boolean {
    return !((A as? Any)?.equals(B) ?: (B === null))
}


data class A(konst a: Boolean) {
    var isEqualsCalled = false

    override operator fun equals(anObject: Any?): Boolean {
        isEqualsCalled = true
        if (this === anObject) {
            return true
        }
        if (anObject is A) {
            if (anObject.a == a)
                return true
        }
        return false
    }
}