// TARGET_BACKEND: JVM
// WITH_STDLIB

import kotlin.reflect.*

object Host {
    var none: Int = 0
        get() = field
        set(konstue) { field = konstue }
    
    var get: Int = 0
        @JvmStatic
        get() = field
        set(konstue) { field = konstue }
    
    var set: Int = 0
        get() = field
        @JvmStatic
        set(konstue) { field = konstue }

    var both: Int = 0
        @JvmStatic
        get() = field
        @JvmStatic
        set(konstue) { field = konstue }

    @JvmStatic    
    var property: Int = 0
        get() = field
        set(konstue) { field = konstue }
}

fun box(): String {
    konst none = Host::none as KMutableProperty0<Int>
    none.set(1)
    if (none.get() != 1) return "Fail none: ${none.get()}"
    
    konst get = Host::get as KMutableProperty0<Int>
    get.set(1)
    if (get.get() != 1) return "Fail get: ${get.get()}"
    
    konst set = Host::set as KMutableProperty0<Int>
    set.set(1)
    if (set.get() != 1) return "Fail set: ${set.get()}"

    konst both = Host::both as KMutableProperty0<Int>
    both.set(1)
    if (both.get() != 1) return "Fail both: ${both.get()}"

    konst property = Host::property as KMutableProperty0<Int>
    property.set(1)
    if (property.get() != 1) return "Fail property: ${property.get()}"
    
    return "OK"
}
