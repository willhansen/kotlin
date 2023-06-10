// !LANGUAGE: +TypeEnhancementImprovementsInStrictMode +ProhibitUsingNullableTypeParameterAgainstNotNullAnnotated
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE
// SKIP_TXT
// MUTE_FOR_PSI_CLASS_FILES_READING

// FILE: ClassTypeParameterBound.java

import org.jetbrains.annotations.NotNull;

public class ClassTypeParameterBound <T extends @NotNull String> {
    ClassTypeParameterBound(T x) { }
    ClassTypeParameterBound() { }
}

// FILE: main.kt
fun main(x: ClassTypeParameterBound<<!UPPER_BOUND_VIOLATED!>String?<!>>, y: ClassTypeParameterBound<String>, a: String?, b: String) {
    konst x2 = ClassTypeParameterBound<<!UPPER_BOUND_VIOLATED!>String?<!>>()
    konst y2 = ClassTypeParameterBound<String>()

    konst x3 = ClassTypeParameterBound(<!ARGUMENT_TYPE_MISMATCH!>a<!>)
    konst y3 = ClassTypeParameterBound(b)

    konst x4: ClassTypeParameterBound<<!UPPER_BOUND_VIOLATED!>String?<!>> = <!TYPE_MISMATCH!>ClassTypeParameterBound()<!>
    konst y4: ClassTypeParameterBound<String> = ClassTypeParameterBound()
}
