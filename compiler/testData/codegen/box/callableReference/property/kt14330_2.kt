var recivier : Any? = "fail"
var konstue2 : Any? = "fail2"

var <T> T.bar : T
    get() = this
    set(konstue) { recivier = this; konstue2 = konstue}


fun box(): String {
    String?::bar.set(null, null)
    if (recivier != null) "fail 1: ${recivier}"
    if (konstue2 != null) "fail 2: ${konstue2}"
    return "OK"
}