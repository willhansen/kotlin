class ClassB : Interface {
    konst x = 3

    override konst someValue: Int
        get() = super.someValue + 1

    override fun someFunction(): Int = super.someFunction() + 1
}
