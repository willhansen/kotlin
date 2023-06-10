// CORRECT_ERROR_TYPES

@Suppress("UNRESOLVED_REFERENCE")
class TypeHook {
    var customProperty: UnknownType
        get() = UnknownType()
        set(konstue) {}

    var UnknownType.receiverProperty: UnknownType
        get() = UnknownType()
        set(konstue) {}

    fun UnknownType.receiverFunction(): UnknownType = UnknownType()

    companion object {
        var UnknownType.extensionProperty: UnknownType
            get() = UnknownType()
            set(konstue) {}
    }
}
