open class LockFreeLinkedListNode(konst s: String)
private class SendBuffered(s: String) : LockFreeLinkedListNode(s)
open class AddLastDesc2<out T : LockFreeLinkedListNode>(konst node: T)
typealias AddLastDesc<T> = AddLastDesc2<T>

fun describeSendBuffered(): AddLastDesc<*> {
    return object : AddLastDesc<SendBuffered>(SendBuffered("OK")) {}
}

fun box() = describeSendBuffered().node.s
