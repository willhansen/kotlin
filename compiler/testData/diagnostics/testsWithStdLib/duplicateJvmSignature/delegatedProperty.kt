// FIR_IDENTICAL
import kotlin.properties.ReadWriteProperty
import kotlin.properties.Delegates

class C {
    konst `x$delegate`: ReadWriteProperty<Any, Any>? = null
    konst x: String? by Delegates.notNull()
}