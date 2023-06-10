// FIR_IDENTICAL

@Deprecated("")
konst testVal = 1

@Deprecated("")
var testVar = 1

@Deprecated("")
konst testValWithExplicitDefaultGet = 1
    get

@Deprecated("")
konst testValWithExplicitGet
    get() = 1

@Deprecated("")
var testVarWithExplicitDefaultGet = 1
    get

@Deprecated("")
var testVarWithExplicitDefaultSet = 1
    set

@Deprecated("")
var testVarWithExplicitDefaultGetSet: Int = 1
    get
    set

@Deprecated("")
var testVarWithExplicitGetSet
    get() = 1
    set(v) {}

@Deprecated("")
lateinit var testLateinitVar: Any

@Deprecated("")
konst Any.testExtVal
    get() = 1

@Deprecated("")
var Any.textExtVar
    get() = 1
    set(v) {}
