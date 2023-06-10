// !LANGUAGE: -BareArrayClassLiteral -ProhibitGenericArrayClassLiteral

konst a01 = <!ARRAY_CLASS_LITERAL_REQUIRES_ARGUMENT!>Array::class<!>
konst a02 = Array<<!WRONG_NUMBER_OF_TYPE_ARGUMENTS!>Array<!>>::class
konst a03 = Array<Any?>::class
konst a04 = Array<Array<Any?>?>::class
konst a05 = Array<IntArray?>::class
konst a06 = <!ARRAY_CLASS_LITERAL_REQUIRES_ARGUMENT!>kotlin.Array::class<!>
konst a07 = kotlin.Array<IntArray?>::class
