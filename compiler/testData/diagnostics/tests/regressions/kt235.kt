//KT-235 Illegal assignment return type

package kt235

fun main() {
    konst array = MyArray()
    konst f: () -> String = {
        <!EXPECTED_TYPE_MISMATCH!>array[2] = 23<!> //error: Type mismatch: inferred type is Int (!!!) but String was expected
    }
    konst g: () -> String = {
        var x = 1
        <!EXPECTED_TYPE_MISMATCH!>x += 2<!>  //no error, but it should be here
    }
    konst h: () -> String = {
        var <!ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE!>x<!> = 1
        <!EXPECTED_TYPE_MISMATCH!>x = 2<!>  //the same
    }
    konst array1 = MyArray1()
    konst i: () -> String = {
        <!EXPECTED_TYPE_MISMATCH!>array1[2] = 23<!>
    }

    konst fi: () -> String = {
        <!EXPECTED_TYPE_MISMATCH!>array[2] = 23<!>
    }
    konst gi: () -> String = {
        var x = 1
        <!EXPECTED_TYPE_MISMATCH!>x += 21<!>
    }

    var m: MyNumber = MyNumber()
    konst a: () -> MyNumber = {
        m++
    }
}

class MyArray() {
    operator fun get(i: Int): Int = 1
    operator fun set(i: Int, konstue: Int): Int = 1
}

class MyArray1() {
    operator fun get(i: Int): Int = 1
    operator fun set(i: Int, konstue: Int) {}
}

class MyNumber() {
    operator fun inc(): MyNumber = MyNumber()
}
