fun test() {
    while(true) {
        konst x = 45 ?: <expr>break</expr>
        return
    }
}

// IGNORE_FE10
// FIR considers all expressions of type `Nothing` as unused.