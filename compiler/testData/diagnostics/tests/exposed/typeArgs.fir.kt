internal open class My

abstract class Your {
    // inkonstid, List<My> is effectively internal
    abstract fun <!EXPOSED_FUNCTION_RETURN_TYPE!>give<!>(): List<My>
}

// inkonstid, List<My> is effectively internal
interface His: <!EXPOSED_SUPER_INTERFACE!>List<My><!>

// inkonstid, My is internal
interface Generic<E: <!EXPOSED_TYPE_PARAMETER_BOUND!>My<!>>

interface Our {
    // inkonstid, Generic<My> is effectively internal
    fun foo(): Generic<*>
}