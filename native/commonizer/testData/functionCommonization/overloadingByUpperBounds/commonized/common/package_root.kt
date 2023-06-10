expect interface I1
expect interface I2
expect interface I3<T>

expect fun <T : I1> functionWithValueParameter(konstue: T): Unit
expect fun <T : I2> functionWithValueParameter(konstue: T): Unit
expect fun <T : I3<String>> functionWithValueParameter(konstue: T): Unit
expect fun <T : I3<in String>> functionWithValueParameter(konstue: T): Unit
expect fun <T : I3<out String>> functionWithValueParameter(konstue: T): Unit
expect fun <T : I3<Int>> functionWithValueParameter(konstue: T): Unit
expect fun <T : I3<in Int>> functionWithValueParameter(konstue: T): Unit
expect fun <T : I3<out Int>> functionWithValueParameter(konstue: T): Unit
expect fun <T : I3<Any>> functionWithValueParameter(konstue: T): Unit
expect fun <T : I3<in Any>> functionWithValueParameter(konstue: T): Unit
expect fun <T : I3<out Any>> functionWithValueParameter(konstue: T): Unit
expect fun <T : I3<Any?>> functionWithValueParameter(konstue: T): Unit
expect fun <T : I3<*>> functionWithValueParameter(konstue: T): Unit
expect fun <T : Any> functionWithValueParameter(konstue: T): Unit
expect fun <T> functionWithValueParameter(konstue: T): Unit
expect fun functionWithValueParameter(konstue: I1): Unit
expect fun functionWithValueParameter(konstue: I2): Unit
expect fun functionWithValueParameter(konstue: Any): Unit
expect fun functionWithValueParameter(konstue: Any?): Unit
expect fun functionWithValueParameter(): Unit

expect fun <T : I1> T.functionWithReceiver(): Unit
expect fun <T : I2> T.functionWithReceiver(): Unit
expect fun <T : I3<String>> T.functionWithReceiver(): Unit
expect fun <T : I3<in String>> T.functionWithReceiver(): Unit
expect fun <T : I3<out String>> T.functionWithReceiver(): Unit
expect fun <T : I3<Int>> T.functionWithReceiver(): Unit
expect fun <T : I3<in Int>> T.functionWithReceiver(): Unit
expect fun <T : I3<out Int>> T.functionWithReceiver(): Unit
expect fun <T : I3<Any>> T.functionWithReceiver(): Unit
expect fun <T : I3<in Any>> T.functionWithReceiver(): Unit
expect fun <T : I3<out Any>> T.functionWithReceiver(): Unit
expect fun <T : I3<Any?>> T.functionWithReceiver(): Unit
expect fun <T : I3<*>> T.functionWithReceiver(): Unit
expect fun <T : Any> T.functionWithReceiver(): Unit
expect fun <T> T.functionWithReceiver(): Unit
expect fun I1.functionWithReceiver(): Unit
expect fun I2.functionWithReceiver(): Unit
expect fun Any.functionWithReceiver(): Unit
expect fun Any?.functionWithReceiver(): Unit
expect fun functionWithReceiver(): Unit
