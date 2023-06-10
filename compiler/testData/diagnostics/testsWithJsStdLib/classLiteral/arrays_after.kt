// !LANGUAGE: +BareArrayClassLiteral +ProhibitGenericArrayClassLiteral

konst a01 = Array::class
konst a02 = <!CLASS_LITERAL_LHS_NOT_A_CLASS!>Array<<!WRONG_NUMBER_OF_TYPE_ARGUMENTS!>Array<!>>::class<!>
konst a03 = <!CLASS_LITERAL_LHS_NOT_A_CLASS!>Array<Any?>::class<!>
konst a04 = <!CLASS_LITERAL_LHS_NOT_A_CLASS!>Array<Array<Any?>?>::class<!>
konst a05 = <!CLASS_LITERAL_LHS_NOT_A_CLASS!>Array<IntArray?>::class<!>
konst a06 = kotlin.Array::class
konst a07 = <!CLASS_LITERAL_LHS_NOT_A_CLASS!>kotlin.Array<IntArray?>::class<!>
