// WITH_STDLIB

/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-300
 * MAIN LINK: expressions, constant-literals, boolean-literals -> paragraph 1 -> sentence 2
 * NUMBER: 25
 * DESCRIPTION: The use of Boolean literals as the identifier (with backtick) in the konstueArgument.
 * NOTE: this test data is generated by FeatureInteractionTestDataGenerator. DO NOT MODIFY CODE MANUALLY!
 */

fun f1(`true`: Boolean, `false`: Boolean) = `true` && !!!`false`

fun f2(`true`: Boolean): Boolean {
    return !`true`
}

fun f3(vararg `false`: Boolean, `true`: Boolean) = `false`.any { it } && `true`

fun box(): String? {
    if (f1(`true` = false, `false` = true)) return null
    if (!f2(`true` = false && true || true && false)) return null
    if (!f3(`false` = *booleanArrayOf(true, false, false, true), `true` = true)) return null

    return "OK"
}
