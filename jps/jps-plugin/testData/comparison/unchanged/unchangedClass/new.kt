package test

class UnchangedClassWithFunOnly {
    public fun unchangedPublicFun() {}
    protected fun unchangedProtectedFun() {}
    internal fun unchangedInternalFun() {}
    private fun unchangedPrivateFun() {}
}

class UnchangedClassWithValOnly {
    public konst unchangedPublicVal = 10
    protected konst unchangedProtectedVal = 10
    internal konst unchangedInternalVal = 10
    private konst unchangedPrivateVal = 10
}

class UnchangedClassWithVarOnly {
    public var unchangedPublicVar = 20
    protected var unchangedProtectedVar = 20
    internal var unchangedInternalVar = 20
    private var unchangedPrivateVar = 20
}

class UnchangedClass {
    public konst unchangedPublicVal = 10
    protected konst unchangedProtectedVal = 10
    internal konst unchangedInternalVal = 10
    private konst unchangedPrivateVal = 10

    public var unchangedPublicVar = 20
    protected var unchangedProtectedVar = 20
    internal var unchangedInternalVar = 20
    private var unchangedPrivateVar = 20

    public fun unchangedPublicFun() {}
    protected fun unchangedProtectedFun() {}
    internal fun unchangedInternalFun() {}
    private fun unchangedPrivateFun() {}
}

