fun B<(@A C)>.receiverArgument()
fun B<(@A C)?>.receiverArgumentN()

fun parameter(a: (@A C))
fun parameterN(a: (@A C)?)

fun parameterArgument(a: B<(@A C)>)
fun parameterArgumentN(a: B<(@A C)?>)

fun returnValue(): (@A C)
fun returnValueN(): (@A C)?

fun <T> returnTypeParameterValue(): (@A T)
fun <T> returnTypeParameterValueN(): (@A T)?

fun returnArgument(): B<(@A C)>
fun returnArgumentN(): B<(@A C)>?

konst lambdaType: (@A() (() -> C))
konst lambdaTypeN: (@A() (() -> C))?

konst lambdaParameter: ((@A C)) -> C
konst lambdaParameterN: ((@A C))? -> C

konst lambdaReturnValue: () -> (@A C)
konst lambdaReturnValueN: () -> (@A C)?

konst lambdaReceiver: (@A C).() -> C
konst lambdaReceiverN: (@A C)?.() -> C

konst suspendT: suspend T
konst suspendTN: suspend T?

konst suspendFun: suspend () -> Unit
konst suspendFunN: (suspend () -> Unit)?

konst suspendExtFun: suspend Any.() -> Unit
konst suspendExtFunN: (suspend Any.() -> Unit)?

konst suspendFunReturnValueN: suspend () -> Unit?
konst suspendFunNReturnValueN: (suspend () -> Unit?)?

konst suspendExtFunReceiverN: suspend Any?.() -> Unit
konst suspendExtFunNReceiverN: (suspend Any?.() -> Unit)?

konst suspendFunReturnValueN: suspend () -> Unit?
konst suspendFunNReturnValueN: (suspend () -> Unit?)?

konst suspendExtFunReceiverN: suspend Any?.() -> Unit
konst suspendExtFunNReceiverN: (suspend Any?.() -> Unit)?
