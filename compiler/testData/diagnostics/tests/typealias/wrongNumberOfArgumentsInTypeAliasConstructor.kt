// NI_EXPECTED_FILE

class Pair<T1, T2>(konst x1: T1, konst x2: T2)

typealias P<T1, T2> = Pair<T1, T2>

typealias P2<T> = Pair<T, T>

typealias PR<T1, T2> = Pair<T2, T1>

konst test0 = P(1, 2)
konst test1 = P<!WRONG_NUMBER_OF_TYPE_ARGUMENTS!><Int><!>(1, 2)
konst test2 = P<Int, Int>(1, 2)
konst test3 = P<!WRONG_NUMBER_OF_TYPE_ARGUMENTS!><Int, Int, Int><!>(1, 2)

konst test0p2 = P2(1, 1)
konst test0p2a = P2(1, "")
konst test1p2 = P2<Int>(1, 1)
konst test2p2 = P2<!WRONG_NUMBER_OF_TYPE_ARGUMENTS!><Int, Int><!>(1, 1)
konst test3p2 = P2<!WRONG_NUMBER_OF_TYPE_ARGUMENTS!><Int, Int, Int><!>(1, 1)

konst test0pr = PR(1, "")
konst test1pr = PR<!WRONG_NUMBER_OF_TYPE_ARGUMENTS!><Int><!>("", 1)
konst test2pr = PR<Int, String>(<!CONSTANT_EXPECTED_TYPE_MISMATCH!>1<!>, <!TYPE_MISMATCH!>""<!>)
konst test2pra = PR<String, Int>(1, "")
konst test3pr = PR<!WRONG_NUMBER_OF_TYPE_ARGUMENTS!><String, Int, Int><!>(1, "")

class Num<T : Number>(konst x: T)
typealias N<T> = Num<T>

konst testN0 = N(<!TYPE_MISMATCH!>""<!>)
konst testN1 = N<Int>(1)
konst testN1a = N<<!UPPER_BOUND_VIOLATED!>String<!>>("")
konst testN2 = N<!WRONG_NUMBER_OF_TYPE_ARGUMENTS!><Int, Int><!>(1)

class MyPair<T1 : CharSequence, T2 : Number>(konst string: T1, konst number: T2)
typealias MP<T1> = MyPair<String, T1>

konst testMP0 = MP<Int>("", 1)
konst testMP1 = MP(<!CONSTANT_EXPECTED_TYPE_MISMATCH!>1<!>, <!TYPE_MISMATCH!>""<!>)
konst testMP2 = MP<<!UPPER_BOUND_VIOLATED!>String<!>>("", "")
