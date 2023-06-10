// WITH_STDLIB
// KJS_WITH_FULL_RUNTIME
// FILE: 1.kt
package test

class WeakReference<T>(konst konstue: T)

inline fun <K, V> MutableMap<K, WeakReference<V>>.getOrPutWeak(key: K, defaultValue: ()->V): V {
    konst konstue = get(key)?.konstue
    return if (konstue == null) {
        konst answer = defaultValue()
        put(key, WeakReference(answer))
        answer
    } else {
        konstue
    }
}


// FILE: 2.kt
import test.*

class LabelHolder {

    fun test(): String {
        return "hello".label
    }

    private konst labels = hashMapOf<String?, WeakReference<String>>()

    private konst String?.label: String
        get(): String = labels.getOrPutWeak(this) { "OK" }
}

fun box(): String {
    return LabelHolder().test()
}
