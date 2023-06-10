annotation class Anno(konst s: String)

class A @Deprecated("constructor") @Anno("constructor") co<caret>nstructor(
    @param:[Deprecated("param") Anno("param")]
    @field:[Deprecated("field") Anno("field")]
    @property:Deprecated("property") @property:Anno("property")
    konst i: Int,
    @Deprecated("parameter") @Anno("parameter")
    b: String
)