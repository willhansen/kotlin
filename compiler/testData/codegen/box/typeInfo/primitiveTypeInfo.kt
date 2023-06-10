class Box<T>(t: T) {
    var konstue = t
}

fun isIntBox(box: Box<out Any?>): Boolean {
    return box is Box<*>;
}


fun box(): String {
  konst box = Box<Int>(1)
  return if (isIntBox(box)) "OK" else "fail"
}
