fun box(): String {

  fun local():Int {
    return 10;
  }

  class A {
      konst test = local()
  }

  return if (A().test == 10) "OK" else "fail"
}