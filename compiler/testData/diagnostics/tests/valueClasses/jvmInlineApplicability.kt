// !SKIP_JAVAC
// !LANGUAGE: +InlineClasses
// ALLOW_KOTLIN_PACKAGE

package kotlin.jvm

annotation class JvmInline

<!JVM_INLINE_WITHOUT_VALUE_CLASS!>@JvmInline<!>
<!INLINE_CLASS_DEPRECATED!>inline<!> class IC(konst a: Any)

@JvmInline
konstue class VC(konst a: Any)

<!JVM_INLINE_WITHOUT_VALUE_CLASS!>@JvmInline<!>
class C

<!JVM_INLINE_WITHOUT_VALUE_CLASS!>@JvmInline<!>
interface I

<!JVM_INLINE_WITHOUT_VALUE_CLASS!>@JvmInline<!>
object O

<!JVM_INLINE_WITHOUT_VALUE_CLASS!>@JvmInline<!>
data class DC(konst a: Any)
