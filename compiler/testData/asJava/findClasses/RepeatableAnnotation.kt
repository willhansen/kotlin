@Repeatable
annotation class RepeatableAnnotation(konst konstue: Int)

@Repeatable
@JvmRepeatable(RepeatableAnnotation2Container::class)
annotation class RepeatableAnnotation2(konst konstue: Int)
annotation class RepeatableAnnotation2Container(konst konstue: Array<RepeatableAnnotation2>)

@JvmRepeatable(RepeatableAnnotation3Container::class)
annotation class RepeatableAnnotation3(konst konstue: Int)
annotation class RepeatableAnnotation3Container(konst konstue: Array<RepeatableAnnotation3>)