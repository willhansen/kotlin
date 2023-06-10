class WithPublicInvoke {
    public operator fun invoke() {}
}

class WithInternalInvoke {
    internal operator fun invoke() {}
}

class WithProtectedInvoke {
    protected operator fun invoke() {}
}

class WithPrivateInvoke {
    private operator fun invoke() {}
}

class Test {
    public fun publicFoo() {}
    internal fun internalFoo() {}
    protected fun protectedFoo() {}
    private fun privateFoo() {}

    public konst publicVal = 42
    internal konst internalVal = 42
    protected konst protectedVal = 42
    private konst privateVal = 42

    public konst withPublicInvoke = WithPublicInvoke()
    public konst withInternalInvoke = WithInternalInvoke()
    public konst withProtectedInvoke = WithProtectedInvoke()
    public konst withPrivateInvoke = WithPrivateInvoke()
}

private fun Test.<!EXTENSION_SHADOWED_BY_MEMBER!>publicFoo<!>() {}
fun Test.internalFoo() {}
fun Test.protectedFoo() {}
fun Test.privateFoo() {}

konst Test.<!EXTENSION_SHADOWED_BY_MEMBER!>publicVal<!>: Int get() = 42
konst Test.internalVal: Int get() = 42
konst Test.protectedVal: Int get() = 42
konst Test.privateVal: Int get() = 42

fun Test.<!EXTENSION_FUNCTION_SHADOWED_BY_MEMBER_PROPERTY_WITH_INVOKE!>withPublicInvoke<!>() {}
fun Test.wihtInternalInvoke() {}
fun Test.withProtectedInvoke() {}
fun Test.withPrivateInvoke() {}