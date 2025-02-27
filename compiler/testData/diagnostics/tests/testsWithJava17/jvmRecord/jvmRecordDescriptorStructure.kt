// FIR_DISABLE_LAZY_RESOLVE_CHECKS
// !API_VERSION: 1.5
// !LANGUAGE: +JvmRecordSupport
// JVM_TARGET: 17
// ENABLE_JVM_PREVIEW

<!NON_DATA_CLASS_JVM_RECORD!>@JvmRecord<!>
class BasicRecord(konst x: String)

@JvmRecord
data class BasicDataRecord(konst x: String)

@JvmRecord
data class VarInConstructor(<!JVM_RECORD_NOT_VAL_PARAMETER!>var x: String<!>)

@JvmRecord
class BasicRecordWithSuperClass(konst x: String) : <!ILLEGAL_JAVA_LANG_RECORD_SUPERTYPE!>Record()<!>

