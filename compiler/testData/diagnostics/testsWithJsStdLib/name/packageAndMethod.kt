// FILE: foo.kt

package foo

<!JS_NAME_CLASH!>fun bar()<!> = 23

// FILE: foobar.kt

package foo.bar

konst x = 42
