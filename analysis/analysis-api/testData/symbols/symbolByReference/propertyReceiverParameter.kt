// DO_NOT_CHECK_NON_PSI_SYMBOL_RESTORE_K1
annotation class ReceiverAnnotation
@Target(AnnotationTarget.TYPE)
annotation class ReceiverTypeAnnotation

konst @receiver:ReceiverAnnotation @ReceiverTypeAnnotation Long.prop: Boolean get() = { t<caret>his == 1 }
