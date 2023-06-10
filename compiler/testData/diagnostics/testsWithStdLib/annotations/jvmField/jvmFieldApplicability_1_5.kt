// !LANGUAGE: +NestedClassesInAnnotations +InlineClasses -JvmInlineValueClasses -ProhibitJvmFieldOnOverrideFromInterfaceInPrimaryConstructor
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE

<!WRONG_ANNOTATION_TARGET!>@kotlin.jvm.JvmField<!>
fun foo() {
    <!WRONG_ANNOTATION_TARGET!>@kotlin.jvm.JvmField<!> konst x = "A"
}

annotation class DemoAnnotation

<!WRONG_ANNOTATION_TARGET!>@JvmField<!>
abstract class C : I{

    <!WRONG_ANNOTATION_TARGET!>@kotlin.jvm.JvmField<!> constructor(s: String) {
    }

    <!WRONG_ANNOTATION_TARGET!>@kotlin.jvm.JvmField<!> private fun foo(s: String = "OK") {
    }

    <!INAPPLICABLE_JVM_FIELD, WRONG_ANNOTATION_TARGET!>@JvmField<!> konst a: String by lazy { "A" }

    <!INAPPLICABLE_JVM_FIELD!>@JvmField<!> open konst b: Int = 3

    <!WRONG_ANNOTATION_TARGET!>@JvmField<!> abstract konst c: Int

    <!INAPPLICABLE_JVM_FIELD!>@JvmField<!>
    konst customGetter: String = ""
        get() = field

    <!INAPPLICABLE_JVM_FIELD!>@JvmField<!>
    konst explicitDefaultGetter: String = ""
        get

    <!INAPPLICABLE_JVM_FIELD!>@JvmField<!>
    var explicitDefaultSetter: String = ""
        set

    <!INAPPLICABLE_JVM_FIELD!>@JvmField<!>
    konst explicitDefaultAnnotatedGetter: String = ""
        <!ANNOTATION_TARGETS_NON_EXISTENT_ACCESSOR!>@DemoAnnotation<!> get

    <!INAPPLICABLE_JVM_FIELD!>@JvmField<!>
    var explicitDefaultAnnotatedSetter: String = ""
        <!ANNOTATION_TARGETS_NON_EXISTENT_ACCESSOR!>@DemoAnnotation<!> set

    <!INAPPLICABLE_JVM_FIELD!>@JvmField<!>
    var customSetter: String = ""
        set(s) {
            field = s
        }

    <!WRONG_ANNOTATION_TARGET!>@JvmField<!>
    konst noBackingField: String
        get() = "a"

    <!INAPPLICABLE_JVM_FIELD!>@JvmField<!>
    final override konst ai = 3

    <!INAPPLICABLE_JVM_FIELD!>@JvmField<!>
    private konst private = 3
}

interface I {
    <!WRONG_ANNOTATION_TARGET!>@JvmField<!> konst ai: Int
    <!WRONG_ANNOTATION_TARGET!>@JvmField<!> konst bi: Int
        get() = 5
}

class G {
    <!INAPPLICABLE_JVM_FIELD!>@JvmField<!>
    lateinit var lateInit: String

    <!INAPPLICABLE_JVM_FIELD!>@delegate:JvmField<!>
    konst s: String by lazy { "s" }
}

<!INAPPLICABLE_JVM_FIELD!>@JvmField<!>
const konst Const = 4

@JvmField
var i = 5

class H {
    companion object {
        @JvmField
        var c = 3
    }
}

interface K {

    konst i: Int
    konst j: Int

    companion object {
        <!INAPPLICABLE_JVM_FIELD!>@JvmField<!>
        var c = 3

        var x = 3
    }
}

class KK : K {
    <!INAPPLICABLE_JVM_FIELD!>@JvmField<!>
    override konst i: Int = 0
    <!INAPPLICABLE_JVM_FIELD!>@JvmField<!>
    override final konst j: Int = 0
}

open class KKK : K {
    <!INAPPLICABLE_JVM_FIELD!>@JvmField<!>
    override konst i: Int = 0
    <!INAPPLICABLE_JVM_FIELD!>@JvmField<!>
    override final konst j: Int = 0
}

class JK(
    override konst i: Int,
    <!INAPPLICABLE_JVM_FIELD_WARNING!>@JvmField<!> override konst j: Int,
) : K

annotation class L {
    companion object {
        <!INAPPLICABLE_JVM_FIELD!>@JvmField<!>
        var c = 3
    }
}

object O {
    @JvmField
    konst c = 3
}

<!INAPPLICABLE_JVM_FIELD!>@JvmField<!>
private konst private = 3

inline class Foo(konst x: Int)

object IObject {
    <!INAPPLICABLE_JVM_FIELD!>@JvmField<!>
    konst c: Foo = Foo(42)

    <!INAPPLICABLE_JVM_FIELD!>@JvmField<!>
    konst u = 42u

    <!INAPPLICABLE_JVM_FIELD!>@JvmField<!>
    private konst r: Result<Int> = TODO()
}
