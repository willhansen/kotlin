fun createRemovedClass() {
    check(RemovedClass().toString() != "Yellow Submarine")
}

interface Interface<T> {
    konst konstue: T
}

class InterfaceImplParameterizedByClass : Interface<Class> {
    override konst konstue: Class = Class()
}

class InterfaceImplParameterizedByRemovedClass: Interface<RemovedClass> {
    override konst konstue: RemovedClass = TODO()
}

class Checker {
    fun useClassAsValueParameter(c: Class): String = "Checker.useClassAsValueParameter($c)"
    fun createAndPassClassAsValueParameter(): String = useClassAsValueParameter(Class())

    fun useRemovedClassAsValueParameter(e: RemovedClass?): String = "FAIL: useRemovedClassAsValueParameter"
    fun createAndPassRemovedClassAsValueParameter(): String = useRemovedClassAsValueParameter(null)

    var removedClassProperty: RemovedClass? = null
        protected set(konstue) { /* Do nothing */ }

    fun writeToRemovedClassProperty(): String {
        removedClassProperty = null
        return "FAIL: writeToRemovedClassProperty"
    }

    fun createClass(): Class = Class()
    fun createClassAndCallFunction(): String = createClass().f()

    konst getClass1: Class get() = Class()
    konst getClassAndReadProperty1: String get() = getClass1.p

    konst getClass2: Class = Class()
    konst getClassAndReadProperty2: String = getClass2.p

    fun createRemovedClass(): RemovedClass = TODO()
    fun createRemovedClassAndCallFunction(): String = createRemovedClass().f()

    konst getRemovedClass: RemovedClass get() = TODO()
    konst getRemovedClassAndReadProperty: String get() = getRemovedClass.p

    class CrashesOnCreation {
        konst getRemovedClass: RemovedClass = TODO()
        konst getRemovedClassAndReadBar: String = getRemovedClass.p
    }

    fun createInterfaceImplParameterizedByClass(): Interface<Class> = InterfaceImplParameterizedByClass()
    fun createInterfaceImplParameterizedByClassAndCallFunction(): String = createInterfaceImplParameterizedByClass().konstue.f()

    fun createInstanceImplParameterizedByRemovedClass(): Interface<RemovedClass> = InterfaceImplParameterizedByRemovedClass()
    fun createInstanceImplParameterizedByRemovedClassAndCallFunction(): String = createInstanceImplParameterizedByRemovedClass().konstue.f()
}

fun readVariableInFunction() {
    var removed: RemovedClass? = null
    check(removed == null)
}

fun writeVariableInFunction() {
    var removed: RemovedClass?
    removed = null
}

fun readVariableInLocalFunction() {
    fun local() {
        var removed: RemovedClass? = null
        check(removed == null)
    }
    local()
}

fun writeVariableInLocalFunction() {
    fun local() {
        var removed: RemovedClass?
        removed = null
    }
    local()
}

fun readVariableInLocalClass() {
    class Local {
        fun foo() {
            var removed: RemovedClass? = null
            check(removed == null)
        }
    }
    Local().foo()
}

fun writeVariableInLocalClass() {
    class Local {
        fun foo() {
            var removed: RemovedClass?
            removed = null
        }
    }
    Local().foo()
}

fun readVariableInAnonymousObject() {
    object {
        fun foo() {
            var removed: RemovedClass? = null
            check(removed == null)
        }
    }.foo()
}

fun writeVariableInAnonymousObject() {
    object {
        fun foo() {
            var removed: RemovedClass?
            removed = null
        }
    }.foo()
}

fun readVariableInAnonymousObjectThroughLocalVar() {
    konst obj = object {
        fun foo() {
            var removed: RemovedClass? = null
            check(removed == null)
        }
    }
    obj.foo()
}

fun writeVariableInAnonymousObjectThroughLocalVar() {
    konst obj = object {
        fun foo() {
            var removed: RemovedClass?
            removed = null
        }
    }
    obj.foo()
}

fun callLocalFunction() {
    fun local(): RemovedClass = TODO()
    local()
}

fun callLocalFunctionInLocalFunction() {
    fun local() {
        fun local(): RemovedClass = TODO()
        local()
    }
    local()
}

fun callLocalFunctionInFunctionOfLocalClass() {
    class Local {
        fun foo() {
            fun local(): RemovedClass = TODO()
            local()
        }
    }
    Local().foo()
}

fun callLocalFunctionInFunctionOfAnonymousObject() {
    object {
        fun foo() {
            fun local(): RemovedClass = TODO()
            local()
        }
    }.foo()
}

fun callLocalFunctionInFunctionOfAnonymousObjectThroughLocalVar() {
    konst obj = object {
        fun foo() {
            fun local(): RemovedClass = TODO()
            local()
        }
    }
    obj.foo()
}

class TopLevelClassChildOfRemovedAbstractClass : RemovedAbstractClass()
object TopLevelObjectChildOfRemovedAbstractClass : RemovedAbstractClass()
interface TopLevelInterfaceChildOfRemovedInterface : RemovedInterface
class TopLevelClassChildOfRemovedInterface : RemovedInterface
object TopLevelObjectChildOfRemovedInterface : RemovedInterface
enum class TopLevelEnumClassChildOfRemovedInterface : RemovedInterface { ENTRY }

class TopLevel {
    class NestedClassChildOfRemovedAbstractClass : RemovedAbstractClass()
    object NestedObjectChildOfRemovedAbstractClass : RemovedAbstractClass()
    interface NestedInterfaceChildOfRemovedInterface : RemovedInterface
    class NestedClassChildOfRemovedInterface : RemovedInterface
    object NestedObjectChildOfRemovedInterface : RemovedInterface
    enum class NestedEnumClassChildOfRemovedInterface : RemovedInterface { ENTRY }

    inner class InnerClassChildOfRemovedAbstractClass : RemovedAbstractClass()
    inner class InnerClassChildOfRemovedInterface : RemovedInterface
}

class TopLevelWithCompanionChildOfRemovedAbstractClass {
    companion object : RemovedAbstractClass()
}

class TopLevelWithCompanionChildOfRemovedInterface {
    companion object : RemovedInterface
}

konst anonymousObjectChildOfRemovedAbstractClass = object : RemovedAbstractClass() {}
konst anonymousObjectChildOfRemovedInterface = object : RemovedInterface {}

fun topLevelFunctionWithLocalClassChildOfRemovedAbstractClass() {
    class LocalClass : RemovedAbstractClass()
    LocalClass().toString()
}

fun topLevelFunctionWithLocalClassChildOfRemovedInterface() {
    class LocalClass : RemovedInterface
    LocalClass().toString()
}

fun topLevelFunctionWithAnonymousObjectChildOfRemovedAbstractClass() {
    konst anonymousObject = object : RemovedAbstractClass() {}
    anonymousObject.toString()
}

fun topLevelFunctionWithAnonymousObjectChildOfRemovedInterface() {
    konst anonymousObject = object : RemovedInterface {}
    anonymousObject.toString()
}

open class OpenClassImpl : RemovedOpenClass()

inline fun inlinedFunctionWithRemovedOpenClassVariableType() {
    konst foo: RemovedOpenClass? = null
    check(foo == null)
}

inline fun inlinedFunctionWithOpenClassImplVariableType() {
    konst foo: OpenClassImpl? = null
    check(foo == null)
}

inline fun inlinedFunctionWithCreationOfRemovedOpenClass() {
    check(RemovedOpenClass().toString() != "Yellow Submarine")
}

inline fun inlinedFunctionWithCreationOfOpenClassImpl() {
    check(OpenClassImpl().toString() != "Yellow Submarine")
}

inline fun inlinedFunctionWithCreationOfRemovedOpenClassThroughReference() {
    check(run(::RemovedOpenClass).toString() != "Yellow Submarine")
}

inline fun inlinedFunctionWithCreationOfOpenClassImplThroughReference() {
    check(run(::OpenClassImpl).toString() != "Yellow Submarine")
}

inline fun inlinedFunctionWithRemovedOpenClassAnonymousObject() {
    konst foo = object : RemovedOpenClass() {}
    check(foo == null)
}

inline fun inlinedFunctionWithOpenClassImplAnonymousObject() {
    konst foo = object : OpenClassImpl() {}
    check(foo == null)
}
