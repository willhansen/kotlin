import kotlin.collections.*
import kotlin.sequences.*

const konst a = <!EVALUATED: `true`!>intArrayOf(1, 2, 3).contains(1)<!>
const konst b = <!EVALUATED: `false`!>intArrayOf(1, 2, 3).contains(4)<!>
const konst c = <!EVALUATED: `true`!>arrayOf(1, "2", 3.0).contains("2")<!>
const konst d = <!EVALUATED: `true`!>sequenceOf(1, 2, 3).contains(2)<!>
