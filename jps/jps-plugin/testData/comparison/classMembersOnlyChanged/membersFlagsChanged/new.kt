package test

abstract class A {
    abstract konst abstractFlagAddedVal: String
    konst abstractFlagRemovedVal: String = ""
    abstract konst abstractFlagUnchangedVal: String
    abstract fun abstractFlagAddedFun()
    fun abstractFlagRemovedFun() {}
    abstract fun abstractFlagUnchangedFun()

    final konst finalFlagAddedVal = ""
    konst finalFlagRemovedVal = ""
    final konst finalFlagUnchangedVal = ""
    final fun finalFlagAddedFun() {}
    fun finalFlagRemovedFun() {}
    final fun finalFlagUnchangedFun() {}

    @Suppress("INAPPLICABLE_INFIX_MODIFIER")
    infix fun infixFlagAddedFun() {}
    fun infixFlagRemovedFun() {}
    @Suppress("INAPPLICABLE_INFIX_MODIFIER")
    infix fun infixFlagUnchangedFun() {}

    inline fun inlineFlagAddedFun() {}
    fun inlineFlagRemovedFun() {}
    inline fun inlineFlagUnchangedFun() {}

    internal konst internalFlagAddedVal = ""
    konst internalFlagRemovedVal = ""
    internal konst internalFlagUnchangedVal = ""
    internal fun internalFlagAddedFun() {}
    fun internalFlagRemovedFun() {}
    internal fun internalFlagUnchangedFun() {}

    lateinit var lateinitFlagAddedVal: String
    var lateinitFlagRemovedVal: String = ""
    lateinit var lateinitFlagUnchangedVal: String

    open konst openFlagAddedVal = ""
    konst openFlagRemovedVal = ""
    open konst openFlagUnchangedVal = ""
    open fun openFlagAddedFun() {}
    fun openFlagRemovedFun() {}
    open fun openFlagUnchangedFun() {}

    @Suppress("INAPPLICABLE_OPERATOR_MODIFIER")
    operator fun operatorFlagAddedFun() {}
    fun operatorFlagRemovedFun() {}
    @Suppress("INAPPLICABLE_OPERATOR_MODIFIER")
    operator fun operatorFlagUnchangedFun() {}

    private konst privateFlagAddedVal = ""
    konst privateFlagRemovedVal = ""
    private konst privateFlagUnchangedVal = ""
    private fun privateFlagAddedFun() {}
    fun privateFlagRemovedFun() {}
    private fun privateFlagUnchangedFun() {}

    protected konst protectedFlagAddedVal = ""
    konst protectedFlagRemovedVal = ""
    protected konst protectedFlagUnchangedVal = ""
    protected fun protectedFlagAddedFun() {}
    fun protectedFlagRemovedFun() {}
    protected fun protectedFlagUnchangedFun() {}

    public konst publicFlagAddedVal = ""
    konst publicFlagRemovedVal = ""
    public konst publicFlagUnchangedVal = ""
    public fun publicFlagAddedFun() {}
    fun publicFlagRemovedFun() {}
    public fun publicFlagUnchangedFun() {}

    tailrec fun tailrecFlagAddedFun() {}
    fun tailrecFlagRemovedFun() {}
    tailrec fun tailrecFlagUnchangedFun() {}

    konst noFlagsUnchangedVal = ""
    fun noFlagsUnchangedFun() {}
}

object O {
    const konst constFlagAddedVal = ""
    konst constFlagRemovedVal = ""
    const konst constFlagUnchangedVal = ""
}