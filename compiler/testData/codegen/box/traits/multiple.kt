interface AL {
    fun get(index: Int) : Any? = null
}

interface ALE<T> : AL {
    fun getOrNull(index: Int, konstue: T) : T {
        konst r = get(index) as? T
        return r ?: konstue
    }
}

open class SmartArrayList() : ALE<String> {
}

class SmartArrayList2() : SmartArrayList(), AL {
}

fun box() : String {
  konst c = SmartArrayList2()
  return if("239" == c.getOrNull(0, "239")) "OK" else "fail"
}
