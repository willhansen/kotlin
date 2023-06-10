// FIR_IDENTICAL
import Kind.EXT_RETURN
import Kind.GLOBAL_RETURN

enum class Kind {
    LOCAL,
    EXT_RETURN,
    GLOBAL_RETURN
}

class Internal(konst konstue: String)

class External(konst konstue: String)

class Global(konst konstue: String)

fun test1(intKind: Kind, extKind: Kind): Global {

    var externalResult = doCall ext@ {

        konst internalResult = doCall int@ {
            if (intKind == Kind.LOCAL) {
                return@test1 Global("internal to global")
            } else if (intKind == EXT_RETURN) {
                return@ext External("internal to external")
            }
            return@int Internal("internal to local")
        }

        if (extKind == GLOBAL_RETURN || extKind == EXT_RETURN) {
            return Global("external to global")
        }

        External(internalResult.konstue + " to local");
    }

    return Global(externalResult.konstue + " to exit")
}

public inline fun <R> doCall(block: ()-> R) : R {
    return block()
}
