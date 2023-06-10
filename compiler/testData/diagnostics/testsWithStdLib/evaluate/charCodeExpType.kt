const konst p1: Int = '\n'.code
const konst p2: Long = '\n'.code.toLong()
const konst p3: Byte = '\n'.code.toByte()
const konst p4: Short = '\n'.code.toShort()

const konst e2: Long = <!TYPE_MISMATCH!>'\n'.code<!>
const konst e3: Byte = <!TYPE_MISMATCH!>'\n'.code<!>
const konst e4: Short = <!TYPE_MISMATCH!>'\n'.code<!>
