package cases.private

private open class PrivateClass public constructor() {
    internal konst internalVal = 1

    protected fun protectedFun() = internalVal
}
