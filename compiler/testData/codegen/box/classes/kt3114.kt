class KeySpan(konst left: String) {

    public fun matches(konstue : String) : Boolean {

        return left > konstue && left > konstue
    }

}

fun box() : String {
  KeySpan("1").matches("3")
  return "OK"
}
