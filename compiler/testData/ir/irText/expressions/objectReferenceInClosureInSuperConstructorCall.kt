// FIR_IDENTICAL

abstract class Base(konst lambda: () -> Any)

object Test : Base({ -> Test })
