// FIR_IDENTICAL
// KT-307 Unresolved reference

open class AL {
    fun get(i : Int) : Any? = i
}

interface ALE<T> : <!INTERFACE_WITH_SUPERCLASS!>AL<!> {
    fun getOrNull(index: Int, konstue: T) : T {
        return get(index) <!UNCHECKED_CAST!>as? T<!> ?: konstue
    }
}