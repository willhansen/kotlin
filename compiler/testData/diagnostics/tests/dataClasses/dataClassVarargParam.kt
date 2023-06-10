// FIR_IDENTICAL
data class My(konst x: Int, <!DATA_CLASS_VARARG_PARAMETER!>vararg konst y: String<!>)

data class Your(<!DATA_CLASS_NOT_PROPERTY_PARAMETER, DATA_CLASS_VARARG_PARAMETER!>vararg z: String<!>)
