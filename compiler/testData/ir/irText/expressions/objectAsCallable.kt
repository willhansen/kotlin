// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6

object A

enum class En { X }

operator fun A.invoke(i: Int) = i
operator fun En.invoke(i: Int) = i

konst test1 = A(42)
konst test2 = En.X(42)
