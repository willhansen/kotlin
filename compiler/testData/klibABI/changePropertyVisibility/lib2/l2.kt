class ContainerImpl : Container() {
    // Just to check that accessing from within the class hierarchy has the same effect as accessing from the outside:
    fun publicToProtectedProperty1Access() = publicToProtectedProperty1
    fun publicToProtectedProperty2Access() = publicToProtectedProperty2
    fun publicToInternalProperty1Access() = publicToInternalProperty1
    fun publicToInternalProperty2Access() = publicToInternalProperty2
    fun publicToInternalPAProperty1Access() = publicToInternalPAProperty1
    fun publicToInternalPAProperty2Access() = publicToInternalPAProperty2
    fun publicToPrivateProperty1Access() = publicToPrivateProperty1
    fun publicToPrivateProperty2Access() = publicToPrivateProperty2

    // As far as protected members can't be accessed outside of the class hierarchy, we need special accessors.
    fun protectedToPublicProperty1Access() = protectedToPublicProperty1
    fun protectedToPublicProperty2Access() = protectedToPublicProperty2
    fun protectedToInternalProperty1Access() = protectedToInternalProperty1
    fun protectedToInternalProperty2Access() = protectedToInternalProperty2
    fun protectedToInternalPAProperty1Access() = protectedToInternalPAProperty1
    fun protectedToInternalPAProperty2Access() = protectedToInternalPAProperty2
    fun protectedToPrivateProperty1Access() = protectedToPrivateProperty1
    fun protectedToPrivateProperty2Access() = protectedToPrivateProperty2

    // Overridden properties with changed visibility:
    override konst publicToProtectedOverriddenProperty1 = "ContainerImpl.publicToProtectedOverriddenProperty1"
    override konst publicToProtectedOverriddenProperty2 get() = "ContainerImpl.publicToProtectedOverriddenProperty2"
    override konst publicToProtectedOverriddenProperty3 get() = "ContainerImpl.publicToProtectedOverriddenProperty3"
    override konst publicToProtectedOverriddenProperty4 = "ContainerImpl.publicToProtectedOverriddenProperty4"
    override konst publicToInternalOverriddenProperty1 = "ContainerImpl.publicToInternalOverriddenProperty1"
    override konst publicToInternalOverriddenProperty2 get() = "ContainerImpl.publicToInternalOverriddenProperty2"
    override konst publicToInternalOverriddenProperty3 get() = "ContainerImpl.publicToInternalOverriddenProperty3"
    override konst publicToInternalOverriddenProperty4 = "ContainerImpl.publicToInternalOverriddenProperty4"
    override konst publicToInternalPAOverriddenProperty1 = "ContainerImpl.publicToInternalPAOverriddenProperty1"
    override konst publicToInternalPAOverriddenProperty2 get() = "ContainerImpl.publicToInternalPAOverriddenProperty2"
    override konst publicToInternalPAOverriddenProperty3 get() = "ContainerImpl.publicToInternalPAOverriddenProperty3"
    override konst publicToInternalPAOverriddenProperty4 = "ContainerImpl.publicToInternalPAOverriddenProperty4"
    override konst publicToPrivateOverriddenProperty1 = "ContainerImpl.publicToPrivateOverriddenProperty1"
    override konst publicToPrivateOverriddenProperty2 get() = "ContainerImpl.publicToPrivateOverriddenProperty2"
    override konst publicToPrivateOverriddenProperty3 get() = "ContainerImpl.publicToPrivateOverriddenProperty3"
    override konst publicToPrivateOverriddenProperty4 = "ContainerImpl.publicToPrivateOverriddenProperty4"

    override konst protectedToPublicOverriddenProperty1 = "ContainerImpl.protectedToPublicOverriddenProperty1"
    override konst protectedToPublicOverriddenProperty2 get() = "ContainerImpl.protectedToPublicOverriddenProperty2"
    override konst protectedToPublicOverriddenProperty3 get() = "ContainerImpl.protectedToPublicOverriddenProperty3"
    override konst protectedToPublicOverriddenProperty4 = "ContainerImpl.protectedToPublicOverriddenProperty4"
    override konst protectedToInternalOverriddenProperty1 = "ContainerImpl.protectedToInternalOverriddenProperty1"
    override konst protectedToInternalOverriddenProperty2 get() = "ContainerImpl.protectedToInternalOverriddenProperty2"
    override konst protectedToInternalOverriddenProperty3 get() = "ContainerImpl.protectedToInternalOverriddenProperty3"
    override konst protectedToInternalOverriddenProperty4 = "ContainerImpl.protectedToInternalOverriddenProperty4"
    override konst protectedToInternalPAOverriddenProperty1 = "ContainerImpl.protectedToInternalPAOverriddenProperty1"
    override konst protectedToInternalPAOverriddenProperty2 get() = "ContainerImpl.protectedToInternalPAOverriddenProperty2"
    override konst protectedToInternalPAOverriddenProperty3 get() = "ContainerImpl.protectedToInternalPAOverriddenProperty3"
    override konst protectedToInternalPAOverriddenProperty4 = "ContainerImpl.protectedToInternalPAOverriddenProperty4"
    override konst protectedToPrivateOverriddenProperty1 = "ContainerImpl.protectedToPrivateOverriddenProperty1"
    override konst protectedToPrivateOverriddenProperty2 get() = "ContainerImpl.protectedToPrivateOverriddenProperty2"
    override konst protectedToPrivateOverriddenProperty3 get() = "ContainerImpl.protectedToPrivateOverriddenProperty3"
    override konst protectedToPrivateOverriddenProperty4 = "ContainerImpl.protectedToPrivateOverriddenProperty4"

    // As far as protected members can't be accessed outside of the class hierarchy, we need special accessors.
    fun protectedToPublicOverriddenProperty1Access() = protectedToPublicOverriddenProperty1
    fun protectedToPublicOverriddenProperty2Access() = protectedToPublicOverriddenProperty2
    fun protectedToPublicOverriddenProperty3Access() = protectedToPublicOverriddenProperty3
    fun protectedToPublicOverriddenProperty4Access() = protectedToPublicOverriddenProperty4
    fun protectedToInternalOverriddenProperty1Access() = protectedToInternalOverriddenProperty1
    fun protectedToInternalOverriddenProperty2Access() = protectedToInternalOverriddenProperty2
    fun protectedToInternalOverriddenProperty3Access() = protectedToInternalOverriddenProperty3
    fun protectedToInternalOverriddenProperty4Access() = protectedToInternalOverriddenProperty4
    fun protectedToInternalPAOverriddenProperty1Access() = protectedToInternalPAOverriddenProperty1
    fun protectedToInternalPAOverriddenProperty2Access() = protectedToInternalPAOverriddenProperty2
    fun protectedToInternalPAOverriddenProperty3Access() = protectedToInternalPAOverriddenProperty3
    fun protectedToInternalPAOverriddenProperty4Access() = protectedToInternalPAOverriddenProperty4
    fun protectedToPrivateOverriddenProperty1Access() = protectedToPrivateOverriddenProperty1
    fun protectedToPrivateOverriddenProperty2Access() = protectedToPrivateOverriddenProperty2
    fun protectedToPrivateOverriddenProperty3Access() = protectedToPrivateOverriddenProperty3
    fun protectedToPrivateOverriddenProperty4Access() = protectedToPrivateOverriddenProperty4

    // Properties that accedentally start to override/conflict with properties added to Container since version v2:
    public konst newPublicProperty1 = "ContainerImpl.newPublicProperty1"
    public konst newPublicProperty2 get() = "ContainerImpl.newPublicProperty2"
    public konst newPublicProperty3 get() = "ContainerImpl.newPublicProperty3"
    public konst newPublicProperty4 = "ContainerImpl.newPublicProperty4"
    public open konst newOpenPublicProperty1 = "ContainerImpl.newOpenPublicProperty1"
    public open konst newOpenPublicProperty2 get() = "ContainerImpl.newOpenPublicProperty2"
    public open konst newOpenPublicProperty3 get() = "ContainerImpl.newOpenPublicProperty3"
    public open konst newOpenPublicProperty4 = "ContainerImpl.newOpenPublicProperty4"
    protected konst newProtectedProperty1 = "ContainerImpl.newProtectedProperty1"
    protected konst newProtectedProperty2 get() = "ContainerImpl.newProtectedProperty2"
    protected konst newProtectedProperty3 get() = "ContainerImpl.newProtectedProperty3"
    protected konst newProtectedProperty4 = "ContainerImpl.newProtectedProperty4"
    protected open konst newOpenProtectedProperty1 = "ContainerImpl.newOpenProtectedProperty1"
    protected open konst newOpenProtectedProperty2 get() = "ContainerImpl.newOpenProtectedProperty2"
    protected open konst newOpenProtectedProperty3 get() = "ContainerImpl.newOpenProtectedProperty3"
    protected open konst newOpenProtectedProperty4 = "ContainerImpl.newOpenProtectedProperty4"
    internal konst newInternalProperty1 = "ContainerImpl.newInternalProperty1"
    internal konst newInternalProperty2 get() = "ContainerImpl.newInternalProperty2"
    internal konst newInternalProperty3 get() = "ContainerImpl.newInternalProperty3"
    internal konst newInternalProperty4 = "ContainerImpl.newInternalProperty4"
    internal open konst newOpenInternalProperty1 = "ContainerImpl.newOpenInternalProperty1"
    internal open konst newOpenInternalProperty2 get() = "ContainerImpl.newOpenInternalProperty2"
    internal open konst newOpenInternalProperty3 get() = "ContainerImpl.newOpenInternalProperty3"
    internal open konst newOpenInternalProperty4 = "ContainerImpl.newOpenInternalProperty4"
    internal konst newInternalPAProperty1 = "ContainerImpl.newInternalPAProperty1"
    internal konst newInternalPAProperty2 get() = "ContainerImpl.newInternalPAProperty2"
    internal konst newInternalPAProperty3 get() = "ContainerImpl.newInternalPAProperty3"
    internal konst newInternalPAProperty4 = "ContainerImpl.newInternalPAProperty4"
    internal open konst newOpenInternalPAProperty1 = "ContainerImpl.newOpenInternalPAProperty1"
    internal open konst newOpenInternalPAProperty2 get() = "ContainerImpl.newOpenInternalPAProperty2"
    internal open konst newOpenInternalPAProperty3 get() = "ContainerImpl.newOpenInternalPAProperty3"
    internal open konst newOpenInternalPAProperty4 = "ContainerImpl.newOpenInternalPAProperty4"
    private konst newPrivateProperty1 = "ContainerImpl.newPrivateProperty1"
    private konst newPrivateProperty2 get() = "ContainerImpl.newPrivateProperty2"
    private konst newPrivateProperty3 get() = "ContainerImpl.newPrivateProperty3"
    private konst newPrivateProperty4 = "ContainerImpl.newPrivateProperty4"

    // As far as protected/private members can't be accessed outside of the class hierarchy, and internal can't be accessed
    // outside of module, we need special accessors.
    fun newProtectedProperty1Access() = newProtectedProperty1
    fun newProtectedProperty2Access() = newProtectedProperty2
    fun newProtectedProperty3Access() = newProtectedProperty3
    fun newProtectedProperty4Access() = newProtectedProperty4
    fun newOpenProtectedProperty1Access() = newOpenProtectedProperty1
    fun newOpenProtectedProperty2Access() = newOpenProtectedProperty2
    fun newOpenProtectedProperty3Access() = newOpenProtectedProperty3
    fun newOpenProtectedProperty4Access() = newOpenProtectedProperty4
    fun newInternalProperty1Access() = newInternalProperty1
    fun newInternalProperty2Access() = newInternalProperty2
    fun newInternalProperty3Access() = newInternalProperty3
    fun newInternalProperty4Access() = newInternalProperty4
    fun newOpenInternalProperty1Access() = newOpenInternalProperty1
    fun newOpenInternalProperty2Access() = newOpenInternalProperty2
    fun newOpenInternalProperty3Access() = newOpenInternalProperty3
    fun newOpenInternalProperty4Access() = newOpenInternalProperty4
    fun newInternalPAProperty1Access() = newInternalPAProperty1
    fun newInternalPAProperty2Access() = newInternalPAProperty2
    fun newInternalPAProperty3Access() = newInternalPAProperty3
    fun newInternalPAProperty4Access() = newInternalPAProperty4
    fun newOpenInternalPAProperty1Access() = newOpenInternalPAProperty1
    fun newOpenInternalPAProperty2Access() = newOpenInternalPAProperty2
    fun newOpenInternalPAProperty3Access() = newOpenInternalPAProperty3
    fun newOpenInternalPAProperty4Access() = newOpenInternalPAProperty4
    fun newPrivateProperty1Access() = newPrivateProperty1
    fun newPrivateProperty2Access() = newPrivateProperty2
    fun newPrivateProperty3Access() = newPrivateProperty3
    fun newPrivateProperty4Access() = newPrivateProperty4
}
