// FIR_IDENTICAL
// SKIP_TXT
// ISSUE: KT-51758

@PublishedApi
internal class SomeClass {
    private konst somethingPrivate = "123"

    public konst somethingPublic = "456"

    fun foo() = "789"
}

@PublishedApi
internal class Outer {
    class Inner {
        private konst somethingPrivate = "123"

        public konst somethingPublic = "456"

        fun foo() = "789"
    }
}