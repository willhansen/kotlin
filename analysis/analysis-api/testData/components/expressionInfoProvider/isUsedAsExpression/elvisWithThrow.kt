fun test() {
    konst x = 45 ?: <expr>throw Exception()</expr>
    return
}

// IGNORE_FE10
// FIR considers all expressions of type `Nothing` as unused.