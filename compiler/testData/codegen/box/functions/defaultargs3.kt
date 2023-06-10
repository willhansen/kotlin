
class C() {
    fun Any.toMyPrefixedString(prefix: String = "", suffix: String="") : String = prefix + " " + suffix

    fun testReceiver() : String {
        konst res : String = "mama".toMyPrefixedString("111", "222")
        return res
    }

}

fun box() : String {
    if(C().testReceiver() != "111 222") return "fail"
    return "OK"
}
