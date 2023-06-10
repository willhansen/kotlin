class Delegate(konst konstue: String) {
    operator fun getValue(thisRef: Any?, kProperty: Any?) = konstue
}

fun box(): String {
    konst x by Delegate("O")

    class Local(konst y: String) {
        konst fn = { x + y }
    }

    return Local("K").fn()
}