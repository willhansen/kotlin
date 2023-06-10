// EXPECTED_REACHABLE_NODES: 1281
package foo

fun box(): String {
    konst a: Int? = null
    konst r = a == null
    if (!r || a != null)
        return "wrong result on simple nullable check"

    //force using Kotlin.equals
    konst t = null
    if (t != undefined)
        return "wrong result when compare null and undefined using Kotlin.equals"

    var i = 0;
    fun foo(): Int? = ++i;
    if (foo() == null)
        return "wrong result on nullable check with side effects"

    if (i != 1)
        return "wrong affects when using nullable check with side effects"

    return "OK"
}