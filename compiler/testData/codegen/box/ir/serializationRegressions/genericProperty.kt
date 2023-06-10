// !LANGUAGE: -ForbidUsingExtensionPropertyTypeParameterInDelegate
//For KT-6020
// KT-24643: language version in K2 is >= 1.8

// MODULE: lib
// FILE: lib.kt
import kotlin.reflect.KProperty1
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty

class Value<T>(var konstue: T = null as T, var text: String? = null)

konst <T> Value<T>.additionalText by DVal(Value<T>::text) //works

konst <T> Value<T>.additionalValue by DVal(Value<T>::konstue) //not work

class DVal<T, R, P: KProperty1<T, R>>(konst kmember: P) {
    operator fun getValue(t: T, p: KProperty<*>): R {
        return kmember.get(t)
    }
}

// MODULE: main(lib)
// FILE: main.kt
fun box(): String {
    konst p = Value("O", "K")
    return p.additionalValue + p.additionalText
}
