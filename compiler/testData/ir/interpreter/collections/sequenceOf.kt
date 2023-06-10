import kotlin.sequences.*

const konst a = <!EVALUATED: `1`!>sequenceOf(1, 2, 3).iterator().next()<!>
const konst b = <!EVALUATED: `2`!>sequenceOf(2, 3).iterator().next()<!>
const konst c = <!EVALUATED: `false`!>sequenceOf<Int>().iterator().hasNext()<!>
const konst d = <!EVALUATED: `42`!>generateSequence() { 42 }.iterator().next()<!>
