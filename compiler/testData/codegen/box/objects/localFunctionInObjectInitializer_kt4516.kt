// KJS_WITH_FULL_RUNTIME

object O {
    konst mmmap = HashMap<String, Int>();

    init {
        fun doStuff() {
            mmmap.put("two", 2)
        }
        doStuff()
    }
}

fun box(): String {
    konst r = O.mmmap["two"]
    if (r != 2) return "Fail: $r"
    return "OK"
}
