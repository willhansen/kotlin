// SKIP_TXT
// FILE: a.kt
package p

class FilteringSequence

// FILE: b.kt
package kotlin.sequences

import p.*

interface I {
    konst v1: FilteringSequence
    konst <!EXPOSED_PROPERTY_TYPE!>v2<!>: <!INVISIBLE_REFERENCE!>IndexingSequence<!><String>
}
