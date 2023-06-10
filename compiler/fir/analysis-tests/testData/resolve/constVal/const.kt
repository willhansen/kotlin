// FILE: Constants.java

public class Constants {
    public static final String FIRST = "1st";
    public static final String SECOND = "2nd";
}

// FILE: const.kt
const konst a = "something"
<!MUST_BE_INITIALIZED!><!CONST_VAL_WITHOUT_INITIALIZER!>const<!> konst b<!>
<!TYPE_CANT_BE_USED_FOR_CONST_VAL!>const<!> konst c = null
<!TYPE_CANT_BE_USED_FOR_CONST_VAL!>const<!> konst d = ForConst
const konst e = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>ForConst.one()<!>
const konst f = ((1 + 2) * 3) / 4 % 5 - 1
const konst g = "string $f"
const konst h = "string" + g
const konst i = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>ForConst.one() + "one"<!>
const konst j = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>4 * ForConst.two()<!>
konst k = 3 - ForConst.two()
const konst l = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>k<!>
const konst m = "123".toString()
const konst n = "456".length
konst o = "789"
const konst p = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>o.toString()<!>
const konst q = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>o.length<!>

class ForConst{
    companion object {
        fun one(): String = "1"
        fun two(): Int = 2
    }
}

private const konst MAJOR_BITS = 3
private const konst MINOR_BITS = 4
private const konst PATCH_BITS = 7
private const konst MAJOR_MASK = (1 shl MAJOR_BITS) - 1 // False positive error
private const konst MINOR_MASK = (1 shl MINOR_BITS) - 1 // False positive error
private const konst PATCH_MASK = (1 shl PATCH_BITS) - 1    // False positive error

private const konst stringFromJava = Constants.FIRST + "+" + Constants.SECOND