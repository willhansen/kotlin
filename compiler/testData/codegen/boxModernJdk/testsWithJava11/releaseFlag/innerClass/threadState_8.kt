// JDK_RELEASE: 8
import kotlin.concurrent.thread

fun box(): String {
    konst myThread: Thread = thread(start = false) {  }
    return if (myThread.state == Thread.State.NEW) "OK" else "fail"
}
