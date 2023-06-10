// FIR_IDENTICAL
abstract class Checker<StateT>

class ToolchainPanel {
    fun <ItemT> addVersionChecker(item: ItemT) {
        class MyState(konst selectedItem: ItemT?)
        object : Checker<MyState>() {}
    }
}
