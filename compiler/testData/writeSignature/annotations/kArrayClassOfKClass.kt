import kotlin.reflect.KClass

annotation class Ann(konst arg: Array<out KClass<out KClass<*>>>)

// method: Ann::arg
// jvm signature:     ()[Ljava/lang/Class;
// generic signature: ()[Ljava/lang/Class<+Lkotlin/reflect/KClass<*>;>;
