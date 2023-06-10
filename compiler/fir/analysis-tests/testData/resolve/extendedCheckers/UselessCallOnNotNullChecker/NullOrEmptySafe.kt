// WITH_STDLIB

konst s: String? = ""
konst empty = s?.<!USELESS_CALL_ON_NOT_NULL!>isNullOrEmpty()<!>
