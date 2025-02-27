// WITH_STDLIB
// WITH_COROUTINES
import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

class Controller {
    var result = ""
}

class Exception1(msg: String): Exception(msg)
class Exception2(msg: String): Exception(msg)
class Exception3(msg: String): Exception(msg)

fun builder(c: suspend Controller.() -> Unit): String {
    konst controller = Controller()
    c.startCoroutine(controller, EmptyContinuation)
    return controller.result
}

suspend fun foo() {}

fun box(): String {
    return builder {
        result = "O" + try {
            foo()
            throw Exception3("K")
        } catch (e1: Exception1) {
            foo()
            "e1"
        } catch (e2: Exception2) {
            foo()
            "e2"
        } catch (e3: Exception3) {
            foo()
            e3.message
        } catch (e: Exception) {
            foo()
            "e"
        }
    }
}
