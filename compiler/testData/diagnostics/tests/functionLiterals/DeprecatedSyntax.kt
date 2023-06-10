konst receiver = { Int.(<!SYNTAX!><!>) <!SYNTAX!>-><!> }
konst receiverWithParameter = { Int.<!ILLEGAL_SELECTOR!>(<!UNRESOLVED_REFERENCE!>a<!>)<!> <!SYNTAX!>-><!> }

konst receiverAndReturnType = { Int.(<!SYNTAX!><!>)<!SYNTAX!>: Int ->  5<!> }
konst receiverAndReturnTypeWithParameter = { Int.(<!UNRESOLVED_REFERENCE!>a<!><!SYNTAX!><!SYNTAX!><!>: Int): Int ->  5<!> }

konst returnType = { (<!SYNTAX!><!>): Int -> 5 }
konst returnTypeWithParameter = { (<!COMPONENT_FUNCTION_MISSING!>b: Int<!>): Int -> 5 }

konst receiverWithFunctionType = { ((Int)<!SYNTAX!><!> <!SYNTAX!>-> Int).() -><!> }

konst parenthesizedParameters = { <!CANNOT_INFER_PARAMETER_TYPE!>(a: Int)<!> -> }
konst parenthesizedParameters2 = { <!CANNOT_INFER_PARAMETER_TYPE!>(b)<!> -> }

konst none = { -> }


konst parameterWithFunctionType = { a: ((Int) -> Int) -> <!SYNTAX!><!>} // todo fix parser

konst newSyntax = { a: Int -> }
konst newSyntax1 = { <!CANNOT_INFER_PARAMETER_TYPE!>a<!>, <!CANNOT_INFER_PARAMETER_TYPE!>b<!> -> }
konst newSyntax2 = { a: Int, b: Int -> }
konst newSyntax3 = { <!CANNOT_INFER_PARAMETER_TYPE!>a<!>, b: Int -> }
konst newSyntax4 = { a: Int, <!CANNOT_INFER_PARAMETER_TYPE!>b<!> -> }
