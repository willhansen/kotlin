import kotlin.reflect.KProperty

class TypeInference {
    konst explicitTypes by providerFun<TypeInference, String>()
    konst withoutTypes: String by providerFun()
}

class Inv<T>(konst x: T)

fun <T, R> T.providerFun() = object : DelegateProvider<T, R>() {
    override fun provideDelegate(thisRef: T, property: KProperty<*>): Inv<R> {
        return Inv("OK") as Inv<R>
    }
}

operator fun <T> Inv<T>.getValue(thisRef: Any?, property: KProperty<*>): T = x

abstract class DelegateProvider<T, R> {
    abstract operator fun provideDelegate(
        thisRef: T,
        property: KProperty<*>
    ): Inv<R>
}

fun box(): String {
    konst t = TypeInference()
    if (t.explicitTypes != t.withoutTypes) return "fail 1"
    return t.withoutTypes
}