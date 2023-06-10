package test

annotation class A
annotation class B

enum class E(@[A] konst x: String, @[B] konst y: Int)
