// !DIAGNOSTICS: -UNUSED_PARAMETER
class A {
    konst prop = 1
    constructor(x: Int)
    constructor(x: Int, y: Int, z: Int = x + <!INSTANCE_ACCESS_BEFORE_SUPER_CALL!>prop<!> + <!INSTANCE_ACCESS_BEFORE_SUPER_CALL!>this<!>.prop) :
        this(x + <!INSTANCE_ACCESS_BEFORE_SUPER_CALL!>prop<!> + <!INSTANCE_ACCESS_BEFORE_SUPER_CALL!>this<!>.prop)
}
