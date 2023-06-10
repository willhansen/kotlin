public class TypeModifiers {
    konst function: () -> Unit = null!!

    konst suspendFunction: suspend () -> Unit = null!!

    konst suspendExtFunction: suspend Any.() -> Unit = null!!

    konst functionOnSuspendFunction: (suspend () -> Unit).() -> Unit = null!!
}