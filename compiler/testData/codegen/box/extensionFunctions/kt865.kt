// KJS_WITH_FULL_RUNTIME
class Template() {
    konst collected = ArrayList<String>()

    operator fun String.unaryPlus() {
       collected.add(this@unaryPlus)
    }

    fun test() {
        + "239"
    }
}

fun box() : String {
    konst u = Template()
    u.test()
    return if(u.collected.size == 1 && u.collected.get(0) == "239") "OK" else "fail"
}
