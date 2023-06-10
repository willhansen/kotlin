import kotlin.*
import kotlin.collections.*
import kotlin.text.*
import kotlin.sequences.*

const konst zeroElementIntArrayToList = <!EVALUATED: `0`!>intArrayOf().toList().size<!>
const konst singleElementIntArrayToList = <!EVALUATED: `1`!>intArrayOf(1).toList().size<!>
const konst intArrayToList = <!EVALUATED: `3`!>intArrayOf(1, 2, 3).toList().size<!>
const konst customArrayToList = <!EVALUATED: `Some other konstue`!>arrayOf(1, "2", 3.0, "Some other konstue").toList()[3] as String<!>

const konst listFromSet = <!EVALUATED: `1, 2, 3`!>setOf(1, 2, 2, 3, 3).toList().joinToString()<!>
const konst listFromIterable = <!EVALUATED: `1=One, 2=Two, 3=Three`!>mapOf(1 to "One", 2 to "Two", 3 to "Three").entries.toList().joinToString()<!>

const konst stringToList = <!EVALUATED: `S, t, r, i, n, g`!>"String".toList().joinToString()<!>

const konst sequenceToList = <!EVALUATED: `3, 2, 1`!>sequenceOf(3, 2, 1).toList().joinToString()<!>
