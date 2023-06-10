// FIR_IDENTICAL
// !LANGUAGE: +InlineClasses, -JvmInlineValueClasses
// !DIAGNOSTICS: -UNUSED_PARAMETER, -PLATFORM_CLASS_MAPPED_TO_KOTLIN
// WITH_STDLIB

package kotlin.jvm

annotation class JvmInline

<!VALUE_CLASS_CANNOT_BE_CLONEABLE!>inline<!> class IC0(konst a: Any): Cloneable

@JvmInline
<!VALUE_CLASS_CANNOT_BE_CLONEABLE!>konstue<!> class VC0(konst a: Any): Cloneable

<!VALUE_CLASS_CANNOT_BE_CLONEABLE!>inline<!> class IC1(konst a: Any): java.lang.Cloneable

@JvmInline
<!VALUE_CLASS_CANNOT_BE_CLONEABLE!>konstue<!> class VC1(konst a: Any): java.lang.Cloneable

interface MyCloneable1: Cloneable

<!VALUE_CLASS_CANNOT_BE_CLONEABLE!>inline<!> class IC2(konst a: Any): MyCloneable1

@JvmInline
<!VALUE_CLASS_CANNOT_BE_CLONEABLE!>konstue<!> class VC2(konst a: Any): MyCloneable1

interface MyCloneable2: java.lang.Cloneable

<!VALUE_CLASS_CANNOT_BE_CLONEABLE!>inline<!> class IC3(konst a: Any): MyCloneable2

@JvmInline
<!VALUE_CLASS_CANNOT_BE_CLONEABLE!>konstue<!> class VC3(konst a: Any): MyCloneable2