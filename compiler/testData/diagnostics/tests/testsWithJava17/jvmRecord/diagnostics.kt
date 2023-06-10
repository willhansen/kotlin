// FIR_DISABLE_LAZY_RESOLVE_CHECKS
// FIR_IDENTICAL
// !API_VERSION: 1.5
// !LANGUAGE: +JvmRecordSupport
// SKIP_TXT
// JVM_TARGET: 17
// ENABLE_JVM_PREVIEW

<!NON_DATA_CLASS_JVM_RECORD!>@JvmRecord<!>
class A0

<!NON_DATA_CLASS_JVM_RECORD!>@JvmRecord<!>
class A1 {
    constructor()
}

<!NON_DATA_CLASS_JVM_RECORD!>@JvmRecord<!>
class A2()

<!NON_DATA_CLASS_JVM_RECORD!>@JvmRecord<!>
class A3(name: String)

<!NON_DATA_CLASS_JVM_RECORD!>@JvmRecord<!>
class A4(var name: String)

<!NON_DATA_CLASS_JVM_RECORD!>@JvmRecord<!>
class A5(vararg konst name: String, y: Int)

@JvmRecord
<!NON_FINAL_JVM_RECORD!>open<!> class A6(konst x: String)

@JvmRecord
<!NON_FINAL_JVM_RECORD!>abstract<!> class A7(konst x: String)

@JvmRecord
<!NON_FINAL_JVM_RECORD!>sealed<!> class A8(konst x: String)

@JvmRecord
<!ENUM_JVM_RECORD!>enum<!> class A9(konst x: String) {
    X("");
}

<!NON_DATA_CLASS_JVM_RECORD!>@JvmRecord<!>
class A10(
    konst x: String,
    konst y: Int,
    vararg konst z: Double,
)

fun main() {
    <!LOCAL_JVM_RECORD!>@JvmRecord<!>
    class Local
}

class Outer {
    @JvmRecord
    <!INNER_JVM_RECORD!>inner<!> class Inner(konst name: String)
}

@JvmRecord
data class A11(<!DATA_CLASS_VARARG_PARAMETER, JVM_RECORD_NOT_LAST_VARARG_PARAMETER!>vararg konst x: String<!>, konst y: Int)
