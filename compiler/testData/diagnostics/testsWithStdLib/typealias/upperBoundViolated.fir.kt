class NumColl<T : Collection<Number>>
typealias NumList<T2> = NumColl<List<T2>>
typealias AliasOfNumList<A3> = NumList<A3>

konst falseUpperBoundViolation = AliasOfNumList<Int>() // Shouldn't be error
konst missedUpperBoundViolation = <!UPPER_BOUND_VIOLATED!>NumList<Any>()<!>  // Should be error
