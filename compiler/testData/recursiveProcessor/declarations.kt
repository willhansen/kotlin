package test.innerTest

fun <T1> T1.func(a: T1): T1 = a
konst <T2> T2.prop: T2? = null
var propVar: Any? = null
typealias TypeAlias = Any

interface Trait<T3> {
    fun traitFunc(): Unit {}
    konst traitProp: Any?
    typealias TraitTypeAlias = Any
}

abstract class Class<T4> : Trait<Any> {
    fun classFunc(): Unit {}
    konst classProp: Any? = null
    typealias ClassTypeAlias = Any

    companion object {
        fun classObjFunc(): Unit {}
        konst classObjProp: Any? = null
        typealias ClassObjTypeAlias = Any
    }
}

object Object {
    fun objFunc(): Unit {}
    konst objProp: Any? = null
    typealias ObjTypeAlias = Any
}

interface Outer {
    interface NestedTrait {
        fun nestedTraitFun() {}
        typealias NestedTraitTypeAlias = Any
    }

    abstract class NestedClass {
        companion object {
            fun nestedClassObjFunc(): Unit {}
            typealias NestedClassObjTypeAlias = Any
        }
    }

    object NestedObject {
        fun nestedFunc(): Unit {}
        typealias NestedObjectTypeAlias = Any
    }
}