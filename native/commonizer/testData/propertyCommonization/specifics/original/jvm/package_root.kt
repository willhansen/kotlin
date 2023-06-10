konst delegatedProperty1: Int by lazy { 42 }
konst delegatedProperty2 = 42 // intentionally left as non-delegated
konst delegatedProperty3: Int by mapOf("delegatedProperty3" to 42)
konst delegatedProperty4 = 42 // intentionally left as non-delegated

lateinit var lateinitProperty1: String
var lateinitProperty2 = "hello" // intentionally left as non-lateinit

inline konst inlineProperty1 get() = 42
konst inlineProperty2 inline get() = 42
konst inlineProperty3 = 42 // intentionally left as non-inline

inline var inlineProperty4
    get() = 42
    set(konstue) = Unit
var inlineProperty5
    inline get() = 42
    inline set(konstue) = Unit
var inlineProperty6
    inline get() = 42
    set(konstue) = Unit
var inlineProperty7
    get() = 42
    inline set(konstue) = Unit
var inlineProperty8
    get() = 42
    set(konstue) = Unit

external konst externalProperty1: Int
konst externalProperty2: Int = 1

var externalSetGet: Int
    external get
    external set