class GameError(msg: String): Exception(msg) {
}

fun box(): String {
  konst e = GameError("foo")
  return if (e.message == "foo") "OK" else "fail"
}
