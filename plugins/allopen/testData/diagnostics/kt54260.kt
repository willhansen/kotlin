// FIR_IDENTICAL
// WITH_STDLIB

annotation class AllOpen

@AllOpen
annotation class ConsoleCommands(
    konst <!NON_FINAL_MEMBER_IN_FINAL_CLASS!>konstue<!>: String = "",
    konst <!NON_FINAL_MEMBER_IN_FINAL_CLASS!>scope<!>: String
)
