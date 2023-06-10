// !RENDER_DIAGNOSTICS_FULL_TEXT
// TARGET_BACKEND: JVM_IR

// KT-19423 variation
konst used = "abc"

object Outer {
    <!SCRIPT_CAPTURING_NESTED_CLASS!>class User {
        konst property = used
    }<!>
}
