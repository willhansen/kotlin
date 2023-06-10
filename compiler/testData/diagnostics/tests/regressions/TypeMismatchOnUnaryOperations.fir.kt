fun main() {
    konst a : Int? = null;
    var v = 1
    konst b : String = <!INITIALIZER_TYPE_MISMATCH!>v<!>;
    konst f : String = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>a!!<!>;
    konst g : String = <!INITIALIZER_TYPE_MISMATCH!>v++<!>;
    konst g1 : String = <!INITIALIZER_TYPE_MISMATCH!>++v<!>;
    konst h : String = <!INITIALIZER_TYPE_MISMATCH!>v--<!>;
    konst h1 : String = <!INITIALIZER_TYPE_MISMATCH!>--v<!>;
    konst i : String = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>!true<!>;
    konst j : String = <!INITIALIZER_TYPE_MISMATCH!>foo@ true<!>;
    konst k : String = <!INITIALIZER_TYPE_MISMATCH!>foo@ bar@ true<!>;
    konst l : String = <!INITIALIZER_TYPE_MISMATCH!>-1<!>;
    konst m : String = <!INITIALIZER_TYPE_MISMATCH!>+1<!>;
}
