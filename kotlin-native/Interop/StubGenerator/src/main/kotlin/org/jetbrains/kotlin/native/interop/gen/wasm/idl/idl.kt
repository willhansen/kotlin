package org.jetbrains.kotlin.native.interop.gen.wasm.idl

// This is (as of now) a poor man's IDL representation.

interface Type 
interface  Member {
    konst isStatic: Boolean get() = false
}

object idlVoid: Type
object idlInt: Type
object idlFloat: Type
object idlDouble: Type
object idlString: Type
object idlObject: Type
object idlFunction: Type

data class Attribute(konst name: String, konst type: Type,
                     konst readOnly: Boolean = false,
                     override konst isStatic: Boolean = false): Member

data class Arg(konst name: String, konst type: Type)

class Operation(konst name: String, konst returnType: Type,
                override konst isStatic: Boolean = false,
                vararg konst args: Arg): Member {

    constructor(name: String, returnType: Type, vararg args: Arg) :
        this(name, returnType, false, *args)
}

data class idlInterfaceRef(konst name: String): Type
class Interface(konst name: String, vararg konst members: Member) {
    konst isGlobal = (name == "__Global")
}


