import kotlin.text.*

const konst trimmed = <!EVALUATED: `1`!>"  1  ".trim()<!>
const konst trimmedWithPredicate = <!EVALUATED: `2`!>("  2  " as CharSequence).trim { it.isWhitespace() }.toString()<!>
const konst charSequenceTrim = <!EVALUATED: `3`!>("  3  " as CharSequence).trim().toString()<!>
