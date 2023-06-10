
fun box() {
    lookAtMe { // 1
        konst c = "c" // 4
    } // 5 (nop)
}

inline fun lookAtMe(f: (String) -> Unit) {
    konst a = "a" // 2
    f(a) // 3 before call, 6 after call (nop)
} // 7 (nop)

// 3 NOP
