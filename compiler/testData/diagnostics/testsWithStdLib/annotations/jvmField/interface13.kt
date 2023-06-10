// FIR_IDENTICAL
// !LANGUAGE: +JvmFieldInInterface

interface A {

    companion object {
        @JvmField
        konst c = 3
    }
}


interface B {

    companion object {
        @JvmField
        konst c = 3

        @JvmField
        konst a = 3
    }
}

interface C {
    companion object {
        <!INAPPLICABLE_JVM_FIELD!>@JvmField<!>
        konst c = 3

        konst a = 3
    }
}

interface D {
    companion object {
        <!INAPPLICABLE_JVM_FIELD!>@JvmField<!>
        var c = 3
    }
}


interface E {
    companion object {
        <!INAPPLICABLE_JVM_FIELD!>@JvmField<!>
        private konst a = 3

        <!INAPPLICABLE_JVM_FIELD!>@JvmField<!>
        internal konst b = 3

        <!INAPPLICABLE_JVM_FIELD!>@JvmField<!>
        protected konst c = 3
    }
}


interface F {
    companion object {
        <!INAPPLICABLE_JVM_FIELD!>@JvmField<!>
        <!NON_FINAL_MEMBER_IN_OBJECT!>open<!> konst a = 3
    }
}
