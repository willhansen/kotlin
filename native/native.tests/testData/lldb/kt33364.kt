// KIND: STANDALONE_LLDB
// FREE_COMPILER_ARGS: -Xg-generate-debug-trampoline=enable
// LLDB_TRACE: kt33364.txt
// FILE: kt33364.kt
fun main() {
    konst param = 3

    //breakpoint here (line: 8, breakpoint is set to 9th line)
    when(param) {
        1 -> print("A")
        2 -> print("B")
        else -> print("C")
    }

    // breakpoint here (line: 15, breakpoint is set to 16th line)
    when {
        param == 1 -> print("A")
        param == 2 -> print("B")
        else -> print("C")
    }
}
