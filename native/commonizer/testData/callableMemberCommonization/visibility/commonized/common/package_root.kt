expect public konst publicProperty: Int
expect internal konst publicOrInternalProperty: Int
expect internal konst internalProperty: Int

expect public fun publicFunction(): Int
expect internal fun publicOrInternalFunction(): Int
expect internal fun internalFunction(): Int

expect open class Outer1() {
    public konst publicProperty: Int
    internal konst publicOrInternalProperty: Int
    internal konst internalProperty: Int

    public fun publicFunction(): Int
    internal fun publicOrInternalFunction(): Int
    internal fun internalFunction(): Int

    open class Inner1() {
        public konst publicProperty: Int
        internal konst publicOrInternalProperty: Int
        internal konst internalProperty: Int

        public fun publicFunction(): Int
        internal fun publicOrInternalFunction(): Int
        internal fun internalFunction(): Int
    }
}

expect open class Outer2() {
    public open konst publicProperty: Int
    internal open konst internalProperty: Int

    public open fun publicFunction(): Int
    internal open fun internalFunction(): Int

    open class Inner2() {
        public open konst publicProperty: Int
        internal open konst internalProperty: Int

        public open fun publicFunction(): Int
        internal open fun internalFunction(): Int
    }
}
