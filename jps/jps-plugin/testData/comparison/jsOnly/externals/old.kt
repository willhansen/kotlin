package test

external class ExternalClass {
    fun removedExternalFun()
    konst removedExternalVal: Int
}

class ClassBecameExternal
external class ClassBecameNonExternal

external fun removedExternalFun()
external konst removedExternalVal: Int

fun funBecameExternal() {}
external fun funBecameNonExternal()
