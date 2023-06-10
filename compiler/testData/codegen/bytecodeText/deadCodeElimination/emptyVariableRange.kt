fun foo() {
    return
    // konst xyz has empty live range because everything after return will be removed as dead
    konst xyz = 1
}

// 0 xyz
