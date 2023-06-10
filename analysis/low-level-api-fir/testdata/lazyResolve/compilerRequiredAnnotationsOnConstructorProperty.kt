// MEMBER_NAME_FILTER: i
annotation class Anno(konst s: String)

class <caret>A @Deprecated("constructor") @Anno("constructor") constructor(
    @param:[Deprecated("param") Anno("param")]
    @field:[Deprecated("field") Anno("field")]
    @property:Deprecated("property") @property:Anno("property")
    konst i: Int,
    @Deprecated("parameter") @Anno("parameter")
    b: String
)