package test

abstract class A {
    konst abstractFlagAddedVal: String = ""
    abstract konst abstractFlagRemovedVal: String
    abstract konst abstractFlagUnchangedVal: String
    fun abstractFlagAddedFun() {}
    abstract fun abstractFlagRemovedFun()
    abstract fun abstractFlagUnchangedFun()

    konst finalFlagAddedVal = ""
    final konst finalFlagRemovedVal = ""
    final konst finalFlagUnchangedVal = ""
    fun finalFlagAddedFun() {}
    final fun finalFlagRemovedFun() {}
    final fun finalFlagUnchangedFun() {}

    fun infixFlagAddedFun() {}
    @Suppress("INAPPLICABLE_INFIX_MODIFIER")
    infix fun infixFlagRemovedFun() {}
    @Suppress("INAPPLICABLE_INFIX_MODIFIER")
    infix fun infixFlagUnchangedFun() {}

    fun inlineFlagAddedFun() {}
    inline fun inlineFlagRemovedFun() {}
    inline fun inlineFlagUnchangedFun() {}

    konst internalFlagAddedVal = ""
    internal konst internalFlagRemovedVal = ""
    internal konst internalFlagUnchangedVal = ""
    fun internalFlagAddedFun() {}
    internal fun internalFlagRemovedFun() {}
    internal fun internalFlagUnchangedFun() {}

    var lateinitFlagAddedVal = ""
    lateinit var lateinitFlagRemovedVal: String
    lateinit var lateinitFlagUnchangedVal: String

    konst openFlagAddedVal = ""
    open konst openFlagRemovedVal = ""
    open konst openFlagUnchangedVal = ""
    fun openFlagAddedFun() {}
    open fun openFlagRemovedFun() {}
    open fun openFlagUnchangedFun() {}

    fun operatorFlagAddedFun() {}
    @Suppress("INAPPLICABLE_OPERATOR_MODIFIER")
    operator fun operatorFlagRemovedFun() {}
    @Suppress("INAPPLICABLE_OPERATOR_MODIFIER")
    operator fun operatorFlagUnchangedFun() {}

    konst privateFlagAddedVal = ""
    private konst privateFlagRemovedVal = ""
    private konst privateFlagUnchangedVal = ""
    fun privateFlagAddedFun() {}
    private fun privateFlagRemovedFun() {}
    private fun privateFlagUnchangedFun() {}

    konst protectedFlagAddedVal = ""
    protected konst protectedFlagRemovedVal = ""
    protected konst protectedFlagUnchangedVal = ""
    fun protectedFlagAddedFun() {}
    protected fun protectedFlagRemovedFun() {}
    protected fun protectedFlagUnchangedFun() {}

    konst publicFlagAddedVal = ""
    public konst publicFlagRemovedVal = ""
    public konst publicFlagUnchangedVal = ""
    fun publicFlagAddedFun() {}
    public fun publicFlagRemovedFun() {}
    public fun publicFlagUnchangedFun() {}

    fun tailrecFlagAddedFun() {}
    tailrec fun tailrecFlagRemovedFun() {}
    tailrec fun tailrecFlagUnchangedFun() {}

    konst noFlagsUnchangedVal = ""
    fun noFlagsUnchangedFun() {}
}

object O {
    konst constFlagAddedVal = ""
    const konst constFlagRemovedVal = ""
    const konst constFlagUnchangedVal = ""
}