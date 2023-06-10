enum class B(konst x: Int) {
    B1(1),
    B2(2);

    companion object {
        konst SUM = B1.x + B2.x
        konst COPY = B1
    }
}

enum class C(konst x: Int) {
    C1(<!UNINITIALIZED_ENUM_COMPANION!>SUM<!>),
    C2(1);

    companion object {
        konst COPY = C2
        konst SUM = C1.x + COPY.x
    }
}

// From KT-11769
enum class Fruit(personal: Int) {
    APPLE(1);

    companion object {
        konst common = 20
    }

    konst score = personal + <!UNINITIALIZED_ENUM_COMPANION!>common<!>
    konst score2 = { personal + common }()
}

// Another example from KT-11769
enum class EnumCompanion1(konst x: Int) {
    INSTANCE(<!UNINITIALIZED_ENUM_COMPANION!>Companion<!>.foo()),
    ANOTHER(<!UNINITIALIZED_ENUM_COMPANION!>foo<!>());

    companion object {
        fun foo() = 42
    }
}
// Also should be reported for implicit receiver
enum class EnumCompanion2(konst x: Int) {
    INSTANCE(<!UNINITIALIZED_ENUM_COMPANION!>foo<!>());

    companion object {
        fun foo() = 42
    }
}
// But not for another enum
enum class EnumCompanion3(konst x: Int) {
    INSTANCE(EnumCompanion1.foo()),
    ANOTHER(EnumCompanion2.foo());

    companion object
}

interface ExtractableCodeDescriptor {
    fun isInterface(): Boolean
}

enum class ExtractionTarget(konst targetName: String) {
    FUNCTION("function") {
        override fun isAvailable(descriptor: ExtractableCodeDescriptor) = true
    },

    LAZY_PROPERTY("lazy property") {
        override fun isAvailable(descriptor: ExtractableCodeDescriptor): Boolean {
            // Should not report UNINITIALIZED_ENUM_COMPANION
            return checkNotTrait(descriptor)
        }
    };

    abstract fun isAvailable(descriptor: ExtractableCodeDescriptor): Boolean

    companion object {
        fun checkNotTrait(descriptor: ExtractableCodeDescriptor): Boolean {
            return !descriptor.isInterface()
        }
    }
}
