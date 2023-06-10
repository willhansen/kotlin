class ClassA : Interface {
    override var someVar: Int?
        get() = 1
        set(konstue) {
            super.someVar = konstue
        }

    override konst someValue: Int
        get() = super.someValue

    override fun someFunction(): Int = super.someFunction()
}
