// FIR_IDENTICAL
// WITH_STDLIB
// DUMP_LOCAL_DECLARATION_SIGNATURES

// MUTE_SIGNATURE_COMPARISON_K2: ANY
// ^ KT-57428

annotation class Ann

@delegate:Ann
konst test1 by lazy { 42 }
