// !LANGUAGE: -JvmStaticInInterface
// !DIAGNOSTICS: -UNUSED_VARIABLE
interface B {
    companion object {
        <!JVM_STATIC_NOT_IN_OBJECT_OR_CLASS_COMPANION!>@JvmStatic fun a1()<!> {

        }

        <!JVM_STATIC_NOT_IN_OBJECT_OR_CLASS_COMPANION!>@JvmStatic private fun a2()<!> {

        }

        <!JVM_STATIC_NOT_IN_OBJECT_OR_CLASS_COMPANION!>@JvmStatic protected fun a3()<!> {

        }

        <!JVM_STATIC_NOT_IN_OBJECT_OR_CLASS_COMPANION!>@JvmStatic internal fun a4()<!> {

        }

        <!JVM_STATIC_NOT_IN_OBJECT_OR_CLASS_COMPANION!>@JvmStatic external fun a5()<!>

        <!JVM_STATIC_NOT_IN_OBJECT_OR_CLASS_COMPANION!>@JvmStatic
        var foo<!> = 1

        <!JVM_STATIC_NOT_IN_OBJECT_OR_CLASS_COMPANION!>@JvmStatic
        var foo1<!> = 1
            protected set

        <!JVM_STATIC_NOT_IN_OBJECT_OR_CLASS_COMPANION!>@JvmStatic
        var foo2<!> = 1
            private set

        <!JVM_STATIC_NOT_IN_OBJECT_OR_CLASS_COMPANION!>@JvmStatic
        private var foo3<!> = 1

        <!JVM_STATIC_NOT_IN_OBJECT_OR_CLASS_COMPANION!>@JvmStatic
        protected var foo4<!> = 1

        <!JVM_STATIC_NOT_IN_OBJECT_OR_CLASS_COMPANION!>@JvmStatic
        protected var foo5<!> = 1

        <!JVM_STATIC_NOT_IN_OBJECT_OR_CLASS_COMPANION!>@JvmStatic
        konst foo6<!> = 1

        konst foo7 = 1
            <!JVM_STATIC_NOT_IN_OBJECT_OR_CLASS_COMPANION!>@JvmStatic get<!>

        private var foo8 = 1
        <!JVM_STATIC_NOT_IN_OBJECT_OR_CLASS_COMPANION!>@JvmStatic <!SETTER_VISIBILITY_INCONSISTENT_WITH_PROPERTY_VISIBILITY!>public<!> set<!>

        public var foo9 = 1
        <!JVM_STATIC_NOT_IN_OBJECT_OR_CLASS_COMPANION!>@JvmStatic private set<!>

        <!JVM_STATIC_NOT_IN_OBJECT_OR_CLASS_COMPANION!>@JvmStatic
        konst foo10: Int<!> external get

        konst foo11: Int <!JVM_STATIC_NOT_IN_OBJECT_OR_CLASS_COMPANION!>@JvmStatic external get<!>
    }

}
