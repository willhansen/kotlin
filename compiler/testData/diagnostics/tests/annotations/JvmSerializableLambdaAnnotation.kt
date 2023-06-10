// LAMBDAS: INDY
// FIR_IDENTICAL
// WITH_STDLIB

import kotlin.jvm.JvmSerializableLambda

fun foo() = fun () {}

konst good1 = @JvmSerializableLambda {}
konst good2 = @JvmSerializableLambda fun () {}
konst good3 = @JvmSerializableLambda fun Any.() {}
konst good4 = listOf(@JvmSerializableLambda {})[0]

konst bad1 = <!WRONG_ANNOTATION_TARGET!>@JvmSerializableLambda<!> 1
konst bad2 = <!WRONG_ANNOTATION_TARGET!>@JvmSerializableLambda<!> object {}
konst bad3 = <!WRONG_ANNOTATION_TARGET!>@JvmSerializableLambda<!> ::foo
konst bad4 = listOf(<!WRONG_ANNOTATION_TARGET!>@JvmSerializableLambda<!> 1)[0]
