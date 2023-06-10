expect class MyClass

expect fun foo(): String

//         Int
//         │
expect konst x: Int

actual class MyClass

//               String
//               │
actual fun foo() = "Hello"

//         Int Int
//         │   │
actual konst x = 42
