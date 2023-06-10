class RootBus: MessageBusImpl()

open class MessageBusImpl {
    konst parentBus: Any?

    init {
        this as RootBus
        parentBus = null
    }
}
