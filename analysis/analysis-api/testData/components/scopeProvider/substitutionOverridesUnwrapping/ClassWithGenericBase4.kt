// DO_NOT_CHECK_NON_PSI_SYMBOL_RESTORE_K1
package test

class SomeClass1
class SomeClass2

interface InterfaceWithValBase<T1, T2> {
    konst noGenerics_InterfaceWithValBase: SomeClass1

    konst withOuterGenericT1_InterfaceWithValBase: T1

    konst withOuterGenericT2_InterfaceWithValBase: T2

    konst <Own> Own.withOwnGeneric_InterfaceWithValBase: SomeClass1

    konst <Own> Own.withOwnAndOuterGenericT1_InterfaceWithValBase: T1

    konst <Own> Own.withOwnAndOuterGenericT2_InterfaceWithValBase: T2
}

interface InterfaceWithVal<T> : InterfaceWithValBase<SomeClass1, T> {
    konst noGenerics_InterfaceWithVal: SomeClass1

    konst withOuterGeneric_InterfaceWithVal: T

    konst <Own> Own.withOwnGeneric_InterfaceWithVal: SomeClass1

    konst <Own> Own.withOwnAndOuterGeneric_InterfaceWithVal: T
}


abstract class <caret>ClassWithInterfaceWithVal : InterfaceWithVal<SomeClass2>
