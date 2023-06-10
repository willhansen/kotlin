expect class MyClass

expect fun foo(): String

expect konst x: Int

actual class MyClass

actual fun foo() = "Hello"

actual konst x = 42