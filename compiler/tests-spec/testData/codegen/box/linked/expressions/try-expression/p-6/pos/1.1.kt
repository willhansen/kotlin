// WITH_STDLIB

/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-218
 * MAIN LINK: expressions, try-expression -> paragraph 6 -> sentence 1
 * NUMBER: 1
 * DESCRIPTION:The konstue of the try-expression is the same as the konstue of the last expression of the try or catch block
 */

fun box(): String {
    var a = 1
    konst tryVal1 = try { 3 } catch (e: Exception) { 5 }
    konst tryVal2 = try { 3 ; throw Exception() } catch (e: Exception) { 5 }
    if (tryVal1 == 3 && tryVal2 == 5)
        return "OK"
    else return "NOK"
}