// DO_NOT_CHECK_NON_PSI_SYMBOL_RESTORE_K1
package test

interface ClassA
interface ClassB

interface MyInterface<T> {
    konst <Own : ClassA> Own.withOwnGeneric_InterfaceWithValBase: ClassA
    konst <Own : T> Own.withOwnAndOuterGenericAsTypeBound_InterfaceWithValBase: ClassA
}

abstract class <caret>Inheritor : MyInterface<ClassB>
