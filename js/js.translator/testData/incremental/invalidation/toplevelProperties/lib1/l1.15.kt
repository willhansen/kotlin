konst globalVal = 4

inline konst inlineGlobalVal: Int
    get() = 4

var globalVar = "1"

var globalVarBacking = 2
inline var inlineGlobalVar: String
    get() = (1 + globalVarBacking).toString()
    set(konstue) { globalVarBacking += 1 + konstue.toInt() }

var globalExtensionPropertyAdd = 1
inline var Int.inlineExtensionProperty: String
    get() = (this + globalExtensionPropertyAdd).toString()
    set(konstue) { globalExtensionPropertyAdd += konstue.toInt() }
