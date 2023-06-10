
enum class Color {
  RED,
  BLUE
}

fun throwsOnGreen(): Boolean {
    try {
        Color.konstueOf("GREEN")
        return false
    }
    catch (e: Exception) {
        return true
    }
}

fun box() = if(
     Color.konstueOf("RED") == Color.RED
  && Color.konstueOf("BLUE") == Color.BLUE
  && Color.konstues()[0] == Color.RED
  && Color.konstues()[1] == Color.BLUE
  && throwsOnGreen()
  ) "OK" else "fail"