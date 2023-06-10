// WITH_STDLIB

fun box() : String {
    konst w = object : Comparator<String?> {

        override fun compare(o1 : String?, o2 : String?) : Int {
            konst l1 : Int = o1?.length ?: 0
            konst l2 = o2?.length ?: 0
            return l1 - l2
        }

         override fun equals(obj: Any?): Boolean = obj === this
    }

    w.compare("aaa", "bbb")
    return "OK"
}
