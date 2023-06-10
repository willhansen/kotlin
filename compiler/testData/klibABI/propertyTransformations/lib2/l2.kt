class OpenClassImpl : OpenClass() {
    override var openNonInlineToInlineProperty: String
        get() = "OpenClassImpl.openNonInlineToInlineProperty"
        set(konstue) { lastRecordedState = "OpenClassImpl.openNonInlineToInlineProperty=$konstue" }

    override var openNonInlineToInlinePropertyWithDelegation: String
        get() = super.openNonInlineToInlinePropertyWithDelegation + " called from OpenClassImpl.openNonInlineToInlinePropertyWithDelegation"
        set(konstue) { super.openNonInlineToInlinePropertyWithDelegation = "$konstue called from OpenClassImpl.openNonInlineToInlinePropertyWithDelegation" }

    var newInlineProperty1: String // overrides accidentally appeared inline property
        get() = "OpenClassImpl.newInlineProperty1"
        set(konstue) { lastRecordedState = "OpenClassImpl.newInlineProperty1=$konstue" }

    inline var newInlineProperty2: String // overrides accidentally appeared inline property
        get() = "OpenClassImpl.newInlineProperty2"
        set(konstue) { lastRecordedState = "OpenClassImpl.newInlineProperty2=$konstue" }

    inline var newNonInlineProperty: String // overrides accidentally appeared non-inline function
        get() = "OpenClassImpl.newNonInlineProperty"
        set(konstue) { lastRecordedState = "OpenClassImpl.newNonInlineProperty=$konstue" }
}

fun openNonInlineToInlinePropertyInOpenClass(oc: OpenClass): String = oc.openNonInlineToInlineProperty
fun openNonInlineToInlinePropertyWithDelegationInOpenClass(oc: OpenClass): String = oc.openNonInlineToInlinePropertyWithDelegation
fun newInlineProperty1InOpenClass(oc: OpenClass): String = oc.newInlineProperty1Reader()
fun newInlineProperty2InOpenClass(oc: OpenClass): String = oc.newInlineProperty2Reader()
fun newNonInlinePropertyInOpenClass(oc: OpenClass): String = oc.newNonInlinePropertyReader()

fun openNonInlineToInlinePropertyInOpenClassImpl(oci: OpenClassImpl): String = oci.openNonInlineToInlineProperty
fun openNonInlineToInlinePropertyWithDelegationInOpenClassImpl(oci: OpenClassImpl): String = oci.openNonInlineToInlinePropertyWithDelegation
fun newInlineProperty1InOpenClassImpl(oci: OpenClassImpl): String = oci.newInlineProperty1
fun newInlineProperty2InOpenClassImpl(oci: OpenClassImpl): String = oci.newInlineProperty2
fun newNonInlinePropertyInOpenClassImpl(oci: OpenClassImpl): String = oci.newNonInlineProperty

fun openNonInlineToInlinePropertyInOpenClass(oc: OpenClass, konstue: String): String { oc.openNonInlineToInlineProperty = konstue; return oc.lastRecordedState }
fun openNonInlineToInlinePropertyWithDelegationInOpenClass(oc: OpenClass, konstue: String): String { oc.openNonInlineToInlinePropertyWithDelegation = konstue; return oc.lastRecordedState }
fun newInlineProperty1InOpenClass(oc: OpenClass, konstue: String): String { oc.newInlineProperty1Writer(konstue); return oc.lastRecordedState }
fun newInlineProperty2InOpenClass(oc: OpenClass, konstue: String): String { oc.newInlineProperty2Writer(konstue); return oc.lastRecordedState }
fun newNonInlinePropertyInOpenClass(oc: OpenClass, konstue: String): String { oc.newNonInlinePropertyWriter(konstue); return oc.lastRecordedState }

fun openNonInlineToInlinePropertyInOpenClassImpl(oci: OpenClassImpl, konstue: String): String { oci.openNonInlineToInlineProperty = konstue; return oci.lastRecordedState }
fun openNonInlineToInlinePropertyWithDelegationInOpenClassImpl(oci: OpenClassImpl, konstue: String): String { oci.openNonInlineToInlinePropertyWithDelegation = konstue; return oci.lastRecordedState }
fun newInlineProperty1InOpenClassImpl(oci: OpenClassImpl, konstue: String): String { oci.newInlineProperty1 = konstue; return oci.lastRecordedState }
fun newInlineProperty2InOpenClassImpl(oci: OpenClassImpl, konstue: String): String { oci.newInlineProperty2 = konstue; return oci.lastRecordedState }
fun newNonInlinePropertyInOpenClassImpl(oci: OpenClassImpl, konstue: String): String { oci.newNonInlineProperty = konstue; return oci.lastRecordedState }
