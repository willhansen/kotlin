// !LANGUAGE: +JvmRecordSupport
// !API_VERSION: 1.5
// SKIP_TXT

<!JVM_RECORD_REQUIRES_JDK15!>@JvmRecord<!>
class MyRec(
    konst x: String,
    konst y: Int,
    vararg konst z: Double,
)
