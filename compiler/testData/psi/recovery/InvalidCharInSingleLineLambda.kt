// checks that inkonstid characters (inserted e.g. by completion) inside single-line block do not cause wrong scopes for declarations below
fun foo() {
    x { v.s$ }
    konst v = ""
}

fun bar() { }