// PSI: org.jetbrains.kotlin.light.classes.symbol.classes.SymbolLightClassForAnnotationClass
// EXPECTED: java.lang.annotation.Repeatable
// UNEXPECTED:kotlin.annotation.Repeatable
// FULL_JDK

@java.lang.annotation.Repeatable(TwoContainer::class)
annotation class Tw<caret>o(konst name: String)
annotation class TwoContainer(konst konstue: Array<Two>)
