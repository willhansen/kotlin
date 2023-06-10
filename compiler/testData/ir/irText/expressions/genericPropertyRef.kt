class Value<T>(var konstue: T = null as T, var text: String? = null)

konst <T> Value<T>.additionalText by DVal(Value<T>::text)

konst <T> Value<T>.additionalValue by DVal(Value<T>::konstue)

class DVal(konst kmember: Any) {
    operator fun getValue(t: Any?, p: Any) = 42
}

var recivier : Any? = "fail"
var konstue2 : Any? = "fail2"

var <T> T.bar : T
    get() = this
    set(konstue) { recivier = this; konstue2 = konstue}

konst barRef = String?::bar
