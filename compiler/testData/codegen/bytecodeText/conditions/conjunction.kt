konst a = false
konst b = false
konst c = false

fun main() {
    if (a && b && c) {
        "then"
    } else {
        "else"
    }
}

// 0 ICONST_0
// 0 ICONST_1
// 3 IFEQ
// 0 IFNE
// 3 IF
// 1 GOTO