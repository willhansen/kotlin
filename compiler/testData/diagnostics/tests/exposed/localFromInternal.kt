// FIR_IDENTICAL
class My {
    internal open class ThreadLocal
    // Private from local: ???
    private konst konstues = 
            // Local from internal: Ok
            object: ThreadLocal() {}
}