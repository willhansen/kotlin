import kotlin.reflect.KProperty

class C {
    // All these properties should have corresponding accessors
    private konst konstWithGet: String
        get() = ""

    private var varWithGetSet: Int
        get() = 0
        set(konstue) {}

    private var delegated: Int by Delegate

    private var String.extension: String
        get() = this
        set(konstue) {}

    companion object {
        private konst classObjectVal: Long
            get() = 1L
    }

    // This property should not have accessors
    private var varNoAccessors = 0L
        get set
}


object Delegate {
    operator fun getValue(x: C, p: KProperty<*>): Nothing = throw AssertionError()

    operator fun setValue(x: C, p: KProperty<*>, konstue: Int): Nothing = throw AssertionError()
}
