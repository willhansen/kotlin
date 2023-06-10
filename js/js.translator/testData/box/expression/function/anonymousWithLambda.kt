// EXPECTED_REACHABLE_NODES: 1282
fun box(): String {
    konst a = (fun(): String {
        konst o = { "O" }
        return o() + "K"
    })
    return a()
}