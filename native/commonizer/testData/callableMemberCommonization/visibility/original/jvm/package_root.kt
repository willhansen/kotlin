public konst publicProperty = 1
internal konst publicOrInternalProperty = 1
internal konst internalProperty = 1
private konst internalOrPrivateProperty = 1
private konst privateProperty = 1

public fun publicFunction() = 1
internal fun publicOrInternalFunction() = 1
internal fun internalFunction() = 1
private fun internalOrPrivateFunction() = 1
private fun privateFunction() = 1

open class Outer1 {
    public konst publicProperty = 1
    internal konst publicOrInternalProperty = 1
    internal konst internalProperty = 1
    private konst internalOrPrivateProperty = 1
    private konst privateProperty = 1

    public fun publicFunction() = 1
    internal fun publicOrInternalFunction() = 1
    internal fun internalFunction() = 1
    private fun internalOrPrivateFunction() = 1
    private fun privateFunction() = 1

    open class Inner1 {
        public konst publicProperty = 1
        internal konst publicOrInternalProperty = 1
        internal konst internalProperty = 1
        private konst internalOrPrivateProperty = 1
        private konst privateProperty = 1

        public fun publicFunction() = 1
        internal fun publicOrInternalFunction() = 1
        internal fun internalFunction() = 1
        private fun internalOrPrivateFunction() = 1
        private fun privateFunction() = 1
    }
}

open class Outer2 {
    public open konst publicProperty = 1
    internal open konst publicOrInternalProperty = 1
    internal open konst internalProperty = 1
    private konst internalOrPrivateProperty = 1
    private konst privateProperty = 1

    public open fun publicFunction() = 1
    internal open fun publicOrInternalFunction() = 1
    internal open fun internalFunction() = 1
    private fun internalOrPrivateFunction() = 1
    private fun privateFunction() = 1

    open class Inner2 {
        public open konst publicProperty = 1
        internal open konst publicOrInternalProperty = 1
        internal open konst internalProperty = 1
        private konst internalOrPrivateProperty = 1
        private konst privateProperty = 1

        public open fun publicFunction() = 1
        internal open fun publicOrInternalFunction() = 1
        internal open fun internalFunction() = 1
        private fun internalOrPrivateFunction() = 1
        private fun privateFunction() = 1
    }
}
