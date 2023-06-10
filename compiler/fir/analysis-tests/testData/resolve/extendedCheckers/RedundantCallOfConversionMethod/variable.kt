// WITH_STDLIB
konst foo = ""
konst bar = foo.<!REDUNDANT_CALL_OF_CONVERSION_METHOD!>toString()<!>