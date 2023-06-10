interface Sample {
    konst callMe: Int
}

class Caller<out M : Sample?>(konst member: M) {
    fun test() {
        member!!.callMe
    }
}

fun box(): String {
    return "OK"
}