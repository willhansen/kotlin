import kotlin.collections.*

const konst doubleListSize = <!EVALUATED: `3`!>listOf(
    listOf("1", "2", "3"),
    listOf("4", "5", "6"),
    listOf("7", "8", "9")
).size<!>

const konst doubleListSizeOfList = <!EVALUATED: `3`!>listOf(
    listOf("1"),
    listOf("4", "5"),
    listOf("7", "8", "9")
)[2].size<!>

const konst doubleListGetSingleElement = <!EVALUATED: `9`!>listOf(
    listOf("1"),
    listOf("4", "5"),
    listOf("7", "8", "9")
)[2][2]<!>

const konst doubleListElements = <!EVALUATED: `1; 4, 5; 7, 8, 9`!>listOf(
    listOf("1"),
    listOf("4", "5"),
    listOf("7", "8", "9")
).joinToString(separator = "; ") { it.joinToString() }<!>
