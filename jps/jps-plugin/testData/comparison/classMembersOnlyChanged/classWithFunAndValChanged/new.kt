package test

class ClassWithFunAdded {
    fun added() {}
    public fun unchangedFun() {}
}

class ClassWithFunRemoved {
    public fun unchangedFun() {}
}

class ClassWithValAndFunAddedAndRemoved {
    public konst konstAdded: String = ""
    fun funAdded() {}
    public fun unchangedFun() {}
}

class ClassWithValConvertedToVar {
    public var konstue: Int = 10
    public fun unchangedFun() {}
}

class ClassWithChangedVisiblityForFun1 {
    private fun foo() {}
    public fun unchangedFun() {}
}

class ClassWithChangedVisiblityForFun2 {
    protected fun foo() {}
    public fun unchangedFun() {}
}
