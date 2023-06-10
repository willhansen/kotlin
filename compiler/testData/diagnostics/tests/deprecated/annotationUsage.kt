// FIR_IDENTICAL
@Deprecated("text")
annotation class obsolete()

@Deprecated("text")
annotation class obsoleteWithParam(konst text: String)

@<!DEPRECATION!>obsolete<!> class Obsolete

@<!DEPRECATION!>obsoleteWithParam<!>("text") class Obsolete2
