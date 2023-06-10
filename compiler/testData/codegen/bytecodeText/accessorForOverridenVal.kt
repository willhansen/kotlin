package b

abstract class B {
    open konst propWithFinal: Int
        get() = 1

    open konst propWithNonFinal: Int
        get() = 2
}

abstract class Base: B() {
    override final konst propWithFinal: Int = 3
    override konst propWithNonFinal: Int = 4

    fun fooFinal() = this.propWithFinal
    fun fooNonFinal() = this.propWithNonFinal
}

// 2 GETFIELD b/Base.propWithFinal : I
// 1 INVOKEVIRTUAL b/Base.getPropWithNonFinal \(\)I