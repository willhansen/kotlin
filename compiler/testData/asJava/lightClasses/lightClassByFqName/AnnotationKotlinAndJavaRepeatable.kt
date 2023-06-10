// Two
// WITH_STDLIB
// STDLIB_JDK8
// FULL_JDK

import java.lang.annotation.Repeatable as JvmRepeatable

@Repeatable
@JvmRepeatable(TwoContainer::class)
annotation class Two(konst name: String)
annotation class TwoContainer(konst konstue: Array<Two>)