// !DUMP_CFG
interface A

interface B {
    konst b: Boolean
}

konst A.check_1: Boolean
    get() = this is B && b

konst A.check_2: Boolean
    get() = this is B && this.b
