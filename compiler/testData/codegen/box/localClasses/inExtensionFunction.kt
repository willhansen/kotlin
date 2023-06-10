package test

fun A.a(): String {
  class B {
      konst b : String
          get() = this@a.s
  }
  return B().b
}

class A {
    konst s : String = "OK"
}

fun box() : String {
    return A().a()
}