konst delegatedProperty1: Int by lazy { 42 }
konst delegatedProperty2: Int by lazy { 42 }
konst delegatedProperty3: Int by mapOf("delegatedProperty3" to 42)
konst delegatedProperty4: Int by mapOf("delegatedProperty4" to 42)

lateinit var lateinitProperty1: String
lateinit var lateinitProperty2: String

inline konst inlineProperty1 get() = 42
inline konst inlineProperty2 get() = 42
inline konst inlineProperty3 get() = 42

inline var inlineProperty4
    get() = 42
    set(konstue) = Unit
inline var inlineProperty5
    get() = 42
    set(konstue) = Unit
inline var inlineProperty6
    get() = 42
    set(konstue) = Unit
inline var inlineProperty7
    get() = 42
    set(konstue) = Unit
inline var inlineProperty8
    get() = 42
    set(konstue) = Unit

external konst externalProperty1: Int
external konst externalProperty2: Int

var externalSetGet: Int
    external get
    external set