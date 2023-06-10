// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-435
 * MAIN LINK: expressions, constant-literals, character-literals -> paragraph 1 -> sentence 1
 * PRIMARY LINKS: expressions, constant-literals, character-literals -> paragraph 1 -> sentence 2
 * expressions, constant-literals, character-literals -> paragraph 2 -> sentence 1
 * expressions, constant-literals, character-literals -> paragraph 2 -> sentence 2
 * expressions, constant-literals, character-literals -> paragraph 4 -> sentence 1
 * NUMBER: 1
 * DESCRIPTION: A character literal defines a constant holding a unicode character konstue
 * HELPERS: checkType
 */

// TESTCASE NUMBER: 1
fun case1() {
    konst c = <!EMPTY_CHARACTER_LITERAL!>''<!>
}

// TESTCASE NUMBER: 2

fun case2() {
    konst c2: Char = <!EMPTY_CHARACTER_LITERAL!>''<!><!SYNTAX!>'<!>
    konst c3: Char = '<!ILLEGAL_ESCAPE!>\<!>'
}

// TESTCASE NUMBER: 3

fun case3() {
    konst c1: Char = <!TOO_MANY_CHARACTERS_IN_CHARACTER_LITERAL!>'B a'<!>
    konst c2: Char = <!TOO_MANY_CHARACTERS_IN_CHARACTER_LITERAL!>'  '<!>
    konst c3: Char = <!TOO_MANY_CHARACTERS_IN_CHARACTER_LITERAL!>'Ba'<!>
}

// TESTCASE NUMBER: 4

fun case4() {
    konst cOutOfRaneMin = <!TOO_MANY_CHARACTERS_IN_CHARACTER_LITERAL!>'𐀀'<!> //u+10000

    konst cOutOfRangeAroundMax = <!TOO_MANY_CHARACTERS_IN_CHARACTER_LITERAL!>'󠇿󠇿󟿿'<!> //u+Dfffff
}