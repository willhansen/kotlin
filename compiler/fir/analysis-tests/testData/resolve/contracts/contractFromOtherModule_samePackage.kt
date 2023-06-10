// LANGUAGE: +ContractSyntaxV2
// SKIP_JAVAC
// This directive is needed to skip this test in LazyBodyIsNotTouchedTilContractsPhaseTestGenerated,
//  because it fails to parse module structure of multimodule test

// WITH_STDLIB

// MODULE: lib
package main

import kotlin.contracts.*

fun requireIsTrue(konstue: Boolean) contract [
    returns() implies konstue
] {
    if (!konstue) throw IllegalArgumentException()
}

// MODULE: main(lib)
package main

fun test(s: Any) {
    requireIsTrue(s is String)
    s.length
}
