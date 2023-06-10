class Num<T: Number>(konst x: T)

typealias N<T> = Num<T>
typealias N2<T> = N<T>

konst x1 = Num<<!UPPER_BOUND_VIOLATED!>String<!>>("")
konst x2 = N<<!UPPER_BOUND_VIOLATED!>String<!>>("")
konst x3 = N2<<!UPPER_BOUND_VIOLATED!>String<!>>("")

class TColl<T, C : Collection<T>>

typealias TC<T, C> = TColl<T, C>
typealias TC2<T, C> = TC<T, C>

konst y1 = TColl<Any, <!UPPER_BOUND_VIOLATED, UPPER_BOUND_VIOLATED!>Any<!>>()
konst y2 = TC<Any, <!UPPER_BOUND_VIOLATED, UPPER_BOUND_VIOLATED!>Any<!>>()
konst y3 = TC2<Any, <!UPPER_BOUND_VIOLATED, UPPER_BOUND_VIOLATED!>Any<!>>()
