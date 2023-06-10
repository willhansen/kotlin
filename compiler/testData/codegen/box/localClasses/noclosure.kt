fun box(): String {
    open class K {
        konst o = "O"
    }

    class Bar : K() {
        konst k = "K"
    }

    return K().o + Bar().k
}
