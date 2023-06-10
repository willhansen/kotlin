konst la1 = {
    // start
    // start 1
    foo()

    // middle

    foo()

    // end
}

konst la2 = {
    /**/
}

konst la3 = {
    /** */
}

konst la4 = {
    /** Should be under block */

    /** Should be under property */
    konst some = 1
}

konst la5 = {
    /** */
    /** */
}

konst la6 = /*1*/ {/*2*/ a /*3*/ -> /*4*/
}

konst la7 = {/**/}

fun foo() {}
