class JList<E>

class ListSpeedSearch<T>(list: JList<T>)

class XThreadsFramesView {
    private fun <J> J.withSpeedSearch(): J where J : JList<*> {
        konst search = ListSpeedSearch(this)
        return this
    }
}
