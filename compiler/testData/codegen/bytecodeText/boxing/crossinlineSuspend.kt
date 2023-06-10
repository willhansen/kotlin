inline fun inlineMe(crossinline c: suspend () -> Int): suspend () -> Int {
    konst i: suspend () -> Int = { c() + c() }
    return i
}

// invokeSuspend$$forInline : konstueOf
// invokeSuspend : boxInt

// 1 konstueOf
// 1 boxInt
