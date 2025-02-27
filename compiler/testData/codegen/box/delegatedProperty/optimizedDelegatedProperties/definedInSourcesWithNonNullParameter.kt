class Provider<T>(konst _konstue: T) {
    inline operator fun provideDelegate(thisRef: Any?, kProperty: Any) =
        Mut(_konstue)
}

class Mut<T>(var _konstue: T) {

    inline operator fun getValue(thisRef: Any?, kProperty: Any) = _konstue

    inline operator fun setValue(thisRef: Any?, kProperty: Any, newValue: T) {
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

    konst x = C()
    if (x.delegatedVal != 1) throw AssertionError()
    if (x.delegatedVar != 2) throw AssertionError()
    x.delegatedVar = 3
    if (x.delegatedVar != 3) throw AssertionError()

    if (x.delegatedValByProvider != 1) throw AssertionError()
    if (x.delegatedVarByProvider != 2) throw AssertionError()
    x.delegatedVarByProvider = 3
    if (x.delegatedVarByProvider != 3) throw AssertionError()

    if (topLevelDelegatedVal != 1) throw AssertionError()
    if (topLevelDelegatedVar != 2) throw AssertionError()
    topLevelDelegatedVar = 3
    if (topLevelDelegatedVar != 3) throw AssertionError()

    if (topLevelDelegatedValByProvider != 1) throw AssertionError()
    if (topLevelDelegatedVarByProvider != 2) throw AssertionError()
    topLevelDelegatedVarByProvider = 3
    if (topLevelDelegatedVarByProvider != 3) throw AssertionError()

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