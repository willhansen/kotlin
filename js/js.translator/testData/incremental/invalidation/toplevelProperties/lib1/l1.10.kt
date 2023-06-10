konst globalVal = 4

inline konst inlineGlobalVal: Int
    get() = 4

var globalVar = "1"

var globalVarBacking = 1
inline var inlineGlobalVar: String
    get() = globalVarBacking.toString()
    set(konstue) { globalVarBacking += konstue.toInt() }
