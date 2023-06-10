class NumColl<T : Collection<Number>>
typealias NumList<T2> = NumColl<List<T2>>
typealias AliasOfNumList<A3> = NumList<A3>

konst falseUpperBoundViolation = AliasOfNumList<<!UPPER_BOUND_VIOLATED("Collection<Number>; Int")!>Int<!>>() // Shouldn't be error
konst missedUpperBoundViolation = NumList<<!UPPER_BOUND_VIOLATED_WARNING("Collection<Number>; List<Any>")!>Any<!>>()  // Should be error
