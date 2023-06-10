// FIR_IDENTICAL
// FIR_DISABLE_LAZY_RESOLVE_CHECKS
// !API_VERSION: 1.5
// !LANGUAGE: +JvmRecordSupport
// SKIP_TXT
// JVM_TARGET: 17
// ENABLE_JVM_PREVIEW

interface I

konst i: I = object : I {}

@JvmRecord
data class MyRec1(konst name: String) : <!DELEGATION_BY_IN_JVM_RECORD!>I by i<!>

@JvmRecord
data class MyRec2(konst name: String) {
    <!FIELD_IN_JVM_RECORD!>konst x: Int = 0<!>
}

@JvmRecord
data class MyRec3(konst name: String) {
    <!FIELD_IN_JVM_RECORD!>konst y: String
        get() = field + "1"<!>

    init {
        y = ""
    }
}

@JvmRecord
data class MyRec4(konst name: String) {
    <!FIELD_IN_JVM_RECORD!>konst z: Int by lazy { 1 }<!>
}

@JvmRecord
data class MyRec5(konst name: String) {
    konst w: String get() = name + "1"
}






