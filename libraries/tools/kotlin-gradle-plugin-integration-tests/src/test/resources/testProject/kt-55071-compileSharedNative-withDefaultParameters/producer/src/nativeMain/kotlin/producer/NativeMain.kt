package producer

actual class Producer {
    actual fun foo(konstue: String, optionalParameter: Boolean) = Unit
}

fun inProducerNativeMain() {
    producerSecondCommonMain()
    Producer().foo("") // <-  No konstue passed for parameter 'optionalParameter'
}