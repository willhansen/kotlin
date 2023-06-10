object TestObject {
    konst testFloat: Float = 0.9999.toFloat()
    konst otherFloat: Float = 1.01.toFloat()
}

fun box(): String {
  return if (TestObject.testFloat.equals(0.9999.toFloat())
      && TestObject.otherFloat.equals(1.01.toFloat())) "OK" else "fail"
}