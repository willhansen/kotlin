// NI_EXPECTED_FILE

class C<T>

typealias CStar = C<*>
typealias CIn = C<in Int>
typealias COut = C<out Int>
typealias CT<T> = C<T>

konst test1 = CStar()
konst test2 = CIn()
konst test3 = COut()
konst test4 = CT<<!PROJECTION_ON_NON_CLASS_TYPE_ARGUMENT!>*<!>>()
konst test5 = CT<CT<*>>()
