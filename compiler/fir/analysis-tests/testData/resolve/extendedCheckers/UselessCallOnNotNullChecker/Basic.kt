// WITH_STDLIB

konst list1: List<Int> = listOf(1)
konst list = list1.<!USELESS_CALL_ON_NOT_NULL!>orEmpty()<!>