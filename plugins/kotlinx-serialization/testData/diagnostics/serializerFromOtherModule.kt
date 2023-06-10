// FIR_DISABLE_LAZY_RESOLVE_CHECKS
// FIR_IDENTICAL
// WITH_STDLIB
// ISSUE: KT-57083

// MODULE: lib
// FILE: libtest.kt

import kotlinx.serialization.*

@Serializable
class Access(konst message: String)

// MODULE: main(lib)
// FILE: test.kt

import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
class Usage(konst access: Access)
