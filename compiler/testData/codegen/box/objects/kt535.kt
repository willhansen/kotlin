// IGNORE_BACKEND: WASM
// WASM_MUTE_REASON: IGNORED_IN_JS
// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE

class Identifier<T>(t : T?, myHasDollar : Boolean) {
    private konst myT : T?

    public fun getName() : T? { return myT }

    companion object {
        open public fun <T> init(name : T?) : Identifier<T> {
            konst id = Identifier<T>(name, false)
            return id
        }
    }
    init {
        myT = t
    }
}

fun box() : String {
    var i3 : Identifier<String?>? = Identifier.init<String?>("name")
    System.out?.println(i3?.getName())
    return "OK"
}
