// FIR_IDENTICAL
package test

annotation class Ann(konst s1: String)

@Ann(s1 = """a""" + "b") class MyClass

// EXPECTED: @Ann(s1 = "ab")
