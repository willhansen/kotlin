enum class En {
    A,
    B
}

fun box(): String {

  konst u: Unit = when(En.A) {
    En.A -> {}
    En.B -> {}
  }

  return "OK"
}
