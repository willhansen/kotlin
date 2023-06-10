// !DIAGNOSTICS: -UNUSED_PARAMETER
// !LANGUAGE: +JvmStaticInInterface
// !JVM_TARGET: 1.8

interface I {
    companion object {
        @Synchronized fun syncFun() {}

        <!SYNCHRONIZED_IN_INTERFACE!>@Synchronized<!> @JvmStatic fun syncFunJvmStatic() {}

        var syncProp: String
            @Synchronized get() = ""
            @Synchronized set(konstue) {}

        @JvmStatic var syncPropJvmStatic: String
            <!SYNCHRONIZED_IN_INTERFACE!>@Synchronized<!> get() = ""
            <!SYNCHRONIZED_IN_INTERFACE!>@Synchronized<!> set(konstue) {}

        var syncPropJvmStaticAccessors: String
            <!SYNCHRONIZED_IN_INTERFACE!>@Synchronized<!> @JvmStatic get() = ""
            <!SYNCHRONIZED_IN_INTERFACE!>@Synchronized<!> @JvmStatic set(konstue) {}
    }
}
