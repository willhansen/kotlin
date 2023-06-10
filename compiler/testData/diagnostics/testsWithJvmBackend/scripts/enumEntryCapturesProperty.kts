// !RENDER_DIAGNOSTICS_FULL_TEXT
// TARGET_BACKEND: JVM_IR

// KT-30616
konst foo = "hello"

<!SCRIPT_CAPTURING_ENUM!>enum class Bar(konst s: String) {
    Eleven(s = foo)
}<!>
