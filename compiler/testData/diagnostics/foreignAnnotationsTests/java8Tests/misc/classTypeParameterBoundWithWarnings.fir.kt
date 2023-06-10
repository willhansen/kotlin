// !LANGUAGE: +ProhibitUsingNullableTypeParameterAgainstNotNullAnnotated, -TypeEnhancementImprovementsInStrictMode
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE
// SKIP_TXT
// MUTE_FOR_PSI_CLASS_FILES_READING

// FILE: ClassTypeParameterBoundWithWarnings.java

import org.jetbrains.annotations.NotNull;

public class ClassTypeParameterBoundWithWarnings <T extends @NotNull String> {
    ClassTypeParameterBoundWithWarnings() { }
    ClassTypeParameterBoundWithWarnings(T x) { }
}

// FILE: main.kt
fun main(x: ClassTypeParameterBoundWithWarnings<<!UPPER_BOUND_VIOLATED!>String?<!>>, y: ClassTypeParameterBoundWithWarnings<String>, a: String?, b: String) {
    konst x2 = ClassTypeParameterBoundWithWarnings<<!UPPER_BOUND_VIOLATED!>String?<!>>()
    konst y2 = ClassTypeParameterBoundWithWarnings<String>()

    konst x3 = ClassTypeParameterBoundWithWarnings(<!ARGUMENT_TYPE_MISMATCH!>a<!>)
    konst y3 = ClassTypeParameterBoundWithWarnings(b)

    konst x4: ClassTypeParameterBoundWithWarnings<<!UPPER_BOUND_VIOLATED!>String?<!>> = <!TYPE_MISMATCH!>ClassTypeParameterBoundWithWarnings()<!>
    konst y4: ClassTypeParameterBoundWithWarnings<String> = ClassTypeParameterBoundWithWarnings()
}
