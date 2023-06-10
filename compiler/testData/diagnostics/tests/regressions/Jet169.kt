fun set(key : String, konstue : String) {
  konst a : String? = ""
  when (a) {
    "" -> <!DEBUG_INFO_SMARTCAST!>a<!>.get(0)
    is String, is Any -> <!DEBUG_INFO_SMARTCAST!>a<!>.compareTo("")
    else -> a.toString()
  }
}
