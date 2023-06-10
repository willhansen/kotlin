// WITH_STDLIB
// FULL_JDK

/*
 * KOTLIN CODEGEN BOX SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-313
 * MAIN LINK: expressions, when-expression -> paragraph 5 -> sentence 2
 * NUMBER: 1
 * DESCRIPTION: The else condition is a special condition which ekonstuates to true if none of the branches above it ekonstuated to true.
 * EXCEPTION: compiletime
 */


// FILE: JavaEnum.java
enum JavaEnum {
    Val_1,
    Val_2,
    Val_3,
}

// FILE: KotlinClass.kt
fun box(): String {
    konst z = JavaEnum.Val_3
    konst when1 = when (z) {
        JavaEnum.Val_1 -> { false }
        else -> {true}
        JavaEnum.Val_2 -> { false }
    }

    return "NOK"
}