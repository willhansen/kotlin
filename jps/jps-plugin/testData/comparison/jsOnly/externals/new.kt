package test

external class ExternalClass {
    fun addedExternalFun()
    konst addedExternalVal: Int
}

external class ClassBecameExternal
class ClassBecameNonExternal

external fun addedExternalFun()
external konst addedExternalVal: Int

external fun funBecameExternal()
fun funBecameNonExternal() {}