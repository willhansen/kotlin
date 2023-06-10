// FILE: 1.kt

fun box() = foo()

// FILE: 2.kt

private konst a = "OK"
fun foo() : String {
  return "${a}"
}
