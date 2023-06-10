// FIR_IDENTICAL
package test

annotation class A(konst a: Int = 12, konst b: String = "Test", konst c: String)

@A(a = 12, c = "Hello")
object SomeObject
