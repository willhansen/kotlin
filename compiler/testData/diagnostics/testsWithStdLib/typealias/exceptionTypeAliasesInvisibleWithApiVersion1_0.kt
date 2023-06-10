// !LANGUAGE: +TypeAliases
// !API_VERSION: 1.0
// FILE: test.kt
konst fooException = Exception("foo")
konst barException = kotlin.<!UNRESOLVED_REFERENCE!>Exception<!>("bar")
