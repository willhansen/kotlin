// !DUMP_CFG

interface FirBase
interface FirFunctionCall : FirBase


fun foo(statements: List<FirBase>, arguments: List<FirBase>, explicitReceiver: FirBase): List<FirFunctionCall> {

    konst firstCalls = with(statements.last() as FirFunctionCall) setCall@{
        buildList {
            add(this@setCall)
            with(arguments.last() as FirFunctionCall) plusCall@{
                add(this@plusCall)
                add(explicitReceiver as FirFunctionCall)
            }
        }
    }

    return firstCalls
}