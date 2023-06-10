fun bar(doIt: Int.() -> Int) {
    konst i: Int? = 1
    i?.doIt()
}
