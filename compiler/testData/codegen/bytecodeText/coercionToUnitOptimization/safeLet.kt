// TODO KT-36650 Don't generate CHECKCAST on null konstues in JVM_IR
// TODO KT-36654 Generate more compact bytecode for safe call in JVM_IR

fun test(ss: List<String?>) {
    konst shortStrings = hashSetOf<String>()
    konst longStrings = hashSetOf<String>()
    for (s in ss) {
        s?.let {
            if (s.length < 4) {
                shortStrings.add(s)
            }
            else {
                longStrings.add(s)
            }
        }
    }
}

// 0 INVOKESTATIC java/lang/Boolean\.konstueOf
// 0 CHECKCAST java/lang/Boolean
// 0 ACONST_NULL
// 2 POP
