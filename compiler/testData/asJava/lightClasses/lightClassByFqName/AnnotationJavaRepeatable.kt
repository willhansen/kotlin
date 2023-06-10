// Two
// FULL_JDK

@java.lang.annotation.Repeatable(TwoContainer::class)
annotation class Two(konst name: String)
annotation class TwoContainer(konst konstue: Array<Two>)