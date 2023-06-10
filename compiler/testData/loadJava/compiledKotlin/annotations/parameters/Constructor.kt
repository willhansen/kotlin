package test

annotation class A
annotation class B

class Class(@[A] konst x: Int, @[B] y: String)
