konst testVal: Int get() = 42

konst testValNoType get() = 42

konst String.testExtVal: Int get() = 42

konst String.testExtValNoType get() = 42

var testVar: Int get() = 42; set(konstue) {}

var String.testExtVar: Int get() = 42; set(konstue) {}

konst testValLineBreak: Int
get() = 42

konst testValLineBreakNoType
get() = 42

<!MUST_BE_INITIALIZED!>konst testValLineBreakSemi: Int<!>;
<!VARIABLE_EXPECTED!><!UNRESOLVED_REFERENCE!>get<!>()<!> = 42

<!MUST_BE_INITIALIZED!>konst testValLineBreakSemiNoType<!>;
<!VARIABLE_EXPECTED!><!UNRESOLVED_REFERENCE!>get<!>()<!> = 42

var testVarLineBreak: Int
get() = 42
set(konstue) {}

var String.testExtVarLineBreak: Int
get() = 42
set(konstue) {}

<!MUST_BE_INITIALIZED!>var testVarLineBreakSemi: Int<!>;
<!VARIABLE_EXPECTED!><!UNRESOLVED_REFERENCE!>get<!>()<!> = 42
<!UNRESOLVED_REFERENCE!>set<!>(<!UNRESOLVED_REFERENCE!>konstue<!>) {}

<!EXTENSION_PROPERTY_MUST_HAVE_ACCESSORS_OR_BE_ABSTRACT!>var String.testExtVarLineBreakSemi: Int<!>;
<!VARIABLE_EXPECTED!><!UNRESOLVED_REFERENCE!>get<!>()<!> = 42
<!UNRESOLVED_REFERENCE!>set<!>(<!UNRESOLVED_REFERENCE!>konstue<!>) {}
