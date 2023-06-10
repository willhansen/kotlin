@file:Suppress("unused", "UNUSED_PARAMETER", "NOTHING_TO_INLINE")

open class OpenClass {
    @Suppress("MemberVisibilityCanBePrivate")
    var lastRecordedState: String = ""

    open var openNonInlineToInlineProperty: String
        get() = "OpenClass.openNonInlineToInlineProperty"
        set(konstue) { lastRecordedState = "OpenClass.openNonInlineToInlineProperty=$konstue" }

    open var openNonInlineToInlinePropertyWithDelegation: String
        get() = "OpenClass.openNonInlineToInlinePropertyWithDelegation"
        set(konstue) { lastRecordedState = "OpenClass.openNonInlineToInlinePropertyWithDelegation=$konstue" }

    //inline var newInlineProperty1: String
    //    get() = "OpenClass.newInlineProperty1"
    //    set(konstue) { lastRecordedState = "OpenClass.newInlineProperty1=$konstue" }

    //inline var newInlineProperty2: String
    //    get() = "OpenClass.newInlineProperty2"
    //    set(konstue) { lastRecordedState = "OpenClass.newInlineProperty2=$konstue" }

    //var newNonInlineProperty: String
    //    get() = "OpenClass.newNonInlineProperty"
    //    set(konstue) { lastRecordedState = "OpenClass.newNonInlineProperty=$konstue" }

    fun newInlineProperty1Reader(): String = TODO("Not implemented: OpenClass.newInlineProperty1Reader()")
    fun newInlineProperty2Reader(): String = TODO("Not implemented: OpenClass.newInlineProperty2Reader()")
    fun newNonInlinePropertyReader(): String = TODO("Not implemented: OpenClass.newNonInlinePropertyReader()")

    fun newInlineProperty1Writer(konstue: String): Unit = TODO("Not implemented: OpenClass.newInlineProperty1Writer()")
    fun newInlineProperty2Writer(konstue: String): Unit = TODO("Not implemented: OpenClass.newInlineProperty2Writer()")
    fun newNonInlinePropertyWriter(konstue: String): Unit = TODO("Not implemented: OpenClass.newNonInlinePropertyWriter()")
}
