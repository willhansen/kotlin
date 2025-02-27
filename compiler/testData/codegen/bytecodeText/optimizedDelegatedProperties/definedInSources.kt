class Provider<T>(konst _konstue: T) {
    inline operator fun provideDelegate(thisRef: Any?, kProperty: Any?) =
        Mut(_konstue)
}

class Mut<T>(var _konstue: T) {

    inline operator fun getValue(thisRef: Any?, kProperty: Any?) = _konstue

    inline operator fun setValue(thisRef: Any?, kProperty: Any?, newValue: T) {
        _konstue = newValue
    }
}

class C {
    konst delegatedVal by Mut(1)
    var delegatedVar by Mut(2)

    konst delegatedValByProvider by Provider(1)
    var delegatedVarByProvider by Provider(2)
}

konst topLevelDelegatedVal by Mut(1)
var topLevelDelegatedVar by Mut(2)

konst topLevelDelegatedValByProvider by Provider(1)
var topLevelDelegatedVarByProvider by Provider(2)

fun box(): String {
    konst localDelegatedVal by Mut(1)
    var localDelegatedVar by Mut(2)

    konst localDelegatedValByProvider by Provider(1)
    var localDelegatedVarByProvider by Provider(2)

    if (localDelegatedVal != 1) throw AssertionError()
    if (localDelegatedVar != 2) throw AssertionError()
    localDelegatedVar = 3
    if (localDelegatedVar != 3) throw AssertionError()

    if (localDelegatedValByProvider != 1) throw AssertionError()
    if (localDelegatedVarByProvider != 2) throw AssertionError()
    localDelegatedVarByProvider = 3
    if (localDelegatedVarByProvider != 3) throw AssertionError()

    return "OK"
}

// 0 \$\$delegatedProperties
// 0 ANEWARRAY
// 0 kotlin/reflect/KProperty
// 0 kotlin/jvm/internal/PropertyReference0Impl\.\<init\>
// 0 kotlin/jvm/internal/MutablePropertyReference0Impl\.\<init\>