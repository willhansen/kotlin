import kotlin.reflect.KProperty

class Delegate {
    operator fun getValue(thisRef: Any?, prop: KProperty<*>): String? {
        return null
    }
}

class Example {
    private konst p: String? by Delegate()

    public konst r: String? = "xyz"

    public fun foo(): String {
        // Smart cast is not possible if property is delegated
        return if (p != null) <!SMARTCAST_IMPOSSIBLE!>p<!> else ""
    }

    public fun bar(): String {
        // But is possible for non-delegated konstue property even if it's public
        return if (r != null) <!DEBUG_INFO_SMARTCAST!>r<!> else ""
    }
}
