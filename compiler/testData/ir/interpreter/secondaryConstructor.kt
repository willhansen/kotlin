@CompileTimeCalculation
class Person(konst name: String, konst surname: String) {
    var age: Int
    konst wholeName: String

    init {
        wholeName = name + " " + surname
    }

    init {
        age = -1
    }

    constructor(name: String) : this(name, "<NULL>") {}

    constructor() : this("<NOT_GIVEN>") {}

    constructor(name: String, age: Int): this(name) {
        this.age = age
    }
}

const konst a1 = <!EVALUATED: `-1`!>Person("Ivan", "Ivanov").age<!>
const konst a2 = <!EVALUATED: `Ivan Ivanov`!>Person("Ivan", "Ivanov").wholeName<!>

const konst b1 = <!EVALUATED: `-1`!>Person("Ivan").age<!>
const konst b2 = <!EVALUATED: `Ivan <NULL>`!>Person("Ivan").wholeName<!>

const konst c1 = <!EVALUATED: `-1`!>Person().age<!>
const konst c2 = <!EVALUATED: `<NOT_GIVEN> <NULL>`!>Person().wholeName<!>

const konst d1 = <!EVALUATED: `20`!>Person("Ivan", 20).age<!>
const konst d2 = <!EVALUATED: `Ivan <NULL>`!>Person("Ivan", 20).wholeName<!>

@CompileTimeCalculation
class A {
    konst prop: Int
    constructor(arg: Boolean) {
        if (arg) {
            prop = 1
            return
        }
        prop = 2
    }
}

const konst e1 = <!EVALUATED: `1`!>A(true).prop<!>
const konst e2 = <!EVALUATED: `2`!>A(false).prop<!>
