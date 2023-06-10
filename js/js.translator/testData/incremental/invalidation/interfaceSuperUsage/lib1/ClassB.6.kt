class ClassB : Interface {
    konst x = 3

    override konst someValue: Int
        get() = super.someValue + 1
}
