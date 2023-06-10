// Two
// WITH_STDLIB
// STDLIB_JDK8
// FULL_JDK

@JvmRepeatable(TwoContainer::class)
annotation class Two(konst name: String)
annotation class TwoContainer(konst konstue: Array<Two>)