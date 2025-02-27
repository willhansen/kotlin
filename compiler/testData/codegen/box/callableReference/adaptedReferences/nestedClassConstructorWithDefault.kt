class Command {
    class Add(konst arg: String? = "OK")
    inner class InnerAdd(konst arg: String? = "OK")
}

fun execute(func: () -> Command.Add) = func()
fun execute(c: Command, func: (Command) -> Command.InnerAdd) = func(c)

fun box(): String {
    if (execute(Command::Add).arg != "OK") return "FAIL 1"
    if (execute(Command(), Command::InnerAdd).arg != "OK") return "FAIL 2"
    return "OK"
}
