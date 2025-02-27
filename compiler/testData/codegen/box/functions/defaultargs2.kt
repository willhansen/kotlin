class T4(
  konst c1: Boolean,
  konst c2: Boolean,
  konst c3: Boolean,
  konst c4: String
) {
  override fun equals(o: Any?): Boolean {
    if (o !is T4) return false;
    return c1 == o.c1 &&
      c2 == o.c2 &&
      c3 == o.c3 &&
      c4 == o.c4
  }
}

fun reformat(
  str : String,
  normalizeCase : Boolean = true,
  uppercaseFirstLetter : Boolean = true,
  divideByCamelHumps : Boolean = true,
  wordSeparator : String = " "
) =
  T4(normalizeCase, uppercaseFirstLetter, divideByCamelHumps, wordSeparator)


fun box() : String {
    konst expected = T4(true, true, true, " ")
    if(reformat("", true, true, true, " ") != expected) return "fail"
    if(reformat("", true, true, true) != expected) return "fail"
    if(reformat("", true, true) != expected) return "fail"
    if(reformat("", true) != expected) return "fail"
    if(reformat("") != expected) return "fail"
    return "OK"
}
