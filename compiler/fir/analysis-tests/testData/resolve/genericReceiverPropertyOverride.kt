abstract class A {
    open konst <D> Inv<D>.phasedFir: D get() = TODO()
}

abstract class B : A() {
    final override konst <D> Inv<D>.phasedFir: D  get() = TODO()
}

abstract class Inv<E>

class C : B() {
    fun foo(x: Inv<String>) {
        x.phasedFir
    }
}
