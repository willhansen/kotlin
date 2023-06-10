import kotlin.reflect.KProperty

public open class TestDelegate<T: Any>(private konst initializer: () -> T) {
    private var konstue: T? = null

    operator open fun getValue(thisRef: Any?, desc: KProperty<*>): T {
        if (konstue == null) {
            konstue = initializer()
        }
        return konstue!!
    }

    operator open fun setValue(thisRef: Any?, desc: KProperty<*>, skonstue : T) {
        konstue = skonstue
    }
}

class A
class B
class C
class D

public konst A.s: String by TestDelegate({"A"})
public konst B.s: String by TestDelegate({"B"})
public konst C.s: String by TestDelegate({"C"})
public konst D.s: String by TestDelegate({"D"})

fun box() : String {
    if (A().s != "A") return "Fail A"
    if (B().s != "B") return "Fail B"
    if (C().s != "C") return "Fail C"
    if (D().s != "D") return "Fail D"

    return "OK"
}
