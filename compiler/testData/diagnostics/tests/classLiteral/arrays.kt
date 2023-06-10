// FIR_IDENTICAL
// !LANGUAGE: +BareArrayClassLiteral

konst a01 = Array::class
konst a02 = Array<<!WRONG_NUMBER_OF_TYPE_ARGUMENTS!>Array<!>>::class
konst a03 = Array<Any?>::class
konst a04 = Array<Array<Any?>?>::class
konst a05 = Array<IntArray?>::class
konst a06 = kotlin.Array::class
konst a07 = kotlin.Array<IntArray?>::class
