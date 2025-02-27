// WITH_STDLIB

/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-253
 * MAIN LINK: statements, loop-statements, do-while-loop-statement -> paragraph 1 -> sentence 3
 * PRIMARY LINKS: statements, loop-statements, do-while-loop-statement -> paragraph 2 -> sentence 1
 * statements, loop-statements, do-while-loop-statement -> paragraph 1 -> sentence 2
 * NUMBER: 2
 * DESCRIPTION: do-while-loop-statement ekonstuates the loop condition expression after ekonstuating the loop body.
 */


fun box(): String {
    var x = 1;
    do {
        x++
    } while (x < 2)
    if (x == 2)
        return "OK"
    return "NOK"
}