konst receiver = { Int.(<!SYNTAX!><!>) <!SYNTAX!>-><!> }
konst receiverWithParameter = { Int.<!ILLEGAL_SELECTOR!>(<!UNRESOLVED_REFERENCE!>a<!>)<!> <!SYNTAX!>-><!> }

konst receiverAndReturnType = { Int.(<!SYNTAX!><!>)<!SYNTAX!>: Int ->  5<!> }
konst receiverAndReturnTypeWithParameter = { Int.(<!UNRESOLVED_REFERENCE!>a<!><!SYNTAX!><!SYNTAX!><!>: Int): Int ->  5<!> }

konst returnType = { (<!SYNTAX!><!>): Int -> 5 }
konst returnTypeWithParameter = { <!COMPONENT_FUNCTION_MISSING!>(b: Int): Int<!> -> 5 }

konst receiverWithFunctionType = { ((Int)<!SYNTAX!><!> <!SYNTAX!>-> Int).() -><!> }

konst parenthesizedParameters = { <!COMPONENT_FUNCTION_MISSING!>(a: Int)<!> -> }
konst parenthesizedParameters2 = { <!COMPONENT_FUNCTION_MISSING!>(b)<!> -> }

konst none = { -> }


konst parameterWithFunctionType = { a: ((Int) -> Int) -> <!SYNTAX!><!>} // todo fix parser

konst newSyntax = { a: Int -> }
konst newSyntax1 = { a, b -> }
konst newSyntax2 = { a: Int, b: Int -> }
konst newSyntax3 = { a, b: Int -> }
konst newSyntax4 = { a: Int, b -> }
