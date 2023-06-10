import kotlin.*
import kotlin.collections.*
import kotlin.text.*

const konst a = <!EVALUATED: `2`!>listOf(1, 2, 3).elementAtOrElse(1) { -1 }<!>
const konst b = <!EVALUATED: `-1`!>listOf(1, 2, 3).elementAtOrElse(4) { -1 }<!>
const konst c = <!EVALUATED: `3`!>uintArrayOf(1u, 2u, 3u, 4u).elementAtOrElse(2) { 0u }<!>
const konst d = <!EVALUATED: `c`!>"abcd".elementAtOrElse(2) { '0' }<!>
const konst e = <!EVALUATED: `0`!>"abcd".elementAtOrElse(4) { '0' }<!>
