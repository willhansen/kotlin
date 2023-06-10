// FIR_IDENTICAL
// KT-630 Bad type inference

fun <T : Any> T?.sure() : T = this!!

konst x = "lala".sure()
konst s : String = x
