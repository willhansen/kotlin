// FIR_IDENTICAL

fun foo(): String = ""
konst x = 42

konst test1 = ""
konst test2 = "abc"
konst test3 = """"""
konst test4 = """abc"""
konst test5 = """
abc
"""
konst test6 = "$test1 ${foo()}"

konst test7 = "$test1"
konst test8 = "${foo()}"
konst test9 = "$x"
