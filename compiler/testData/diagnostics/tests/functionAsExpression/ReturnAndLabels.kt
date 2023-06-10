// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE

konst label_fun = label@ fun () {
    return@label
}

konst parenthesized_label_fun = (label@ fun () {
    return@label
})
