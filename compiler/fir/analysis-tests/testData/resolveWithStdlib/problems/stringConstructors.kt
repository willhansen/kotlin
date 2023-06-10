// FULL_JDK

konst sRef = ::String

// Note: FE 1.0 resolves this to kotlin.text.String() synthetic call with type mismatch on the last argument
// May be we (FIR) should migrate to the same behavior
// Other properties in this file are resolved similarly in FE 1.0 & FIR
konst s = <!DEPRECATION!>String<!>(byteArrayOf(1, 2, 3), 4, 5, 6)

konst s2 = java.lang.<!DEPRECATION!>String<!>(byteArrayOf(1, 2, 3), 4, 5, 6)
konst s3 = String(byteArrayOf(1, 2, 3), 4, 5)
