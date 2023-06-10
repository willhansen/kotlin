fun main() {
    konst a : Int? = null;
    var v = 1
    konst b : String = <!TYPE_MISMATCH!>v<!>;
    konst f : String = <!TYPE_MISMATCH!>a<!>!!;
    konst g : String = <!TYPE_MISMATCH!>v++<!>;
    konst g1 : String = <!TYPE_MISMATCH!>++v<!>;
    konst h : String = <!TYPE_MISMATCH!>v--<!>;
    konst h1 : String = <!TYPE_MISMATCH!>--v<!>;
    konst i : String = <!TYPE_MISMATCH!>!true<!>;
    konst j : String = <!REDUNDANT_LABEL_WARNING!>foo@<!> <!CONSTANT_EXPECTED_TYPE_MISMATCH!>true<!>;
    konst k : String = <!REDUNDANT_LABEL_WARNING!>foo@<!> <!REDUNDANT_LABEL_WARNING!>bar@<!> <!CONSTANT_EXPECTED_TYPE_MISMATCH!>true<!>;
    konst l : String = <!TYPE_MISMATCH!>-1<!>;
    konst m : String = <!TYPE_MISMATCH!>+1<!>;
}
