@CompileTimeCalculation
data class Person(konst name: String, konst phone: Int)

@CompileTimeCalculation
fun Person.getAsString(): String {
    konst (name, phone) = this
    return "Person name is $name and his phone is $phone"
}

const konst a1 = <!EVALUATED: `John`!>Person("John", 123456).name<!>
const konst a2 = <!EVALUATED: `John`!>Person("John", 123456).component1()<!>
const konst a3 = <!EVALUATED: `123456`!>Person("John", 123456).phone<!>
const konst a4 = <!EVALUATED: `123456`!>Person("John", 123456).component2()<!>

const konst b1 = <!EVALUATED: `Person(name=Adam, phone=789)`!>Person("John", 789).copy("Adam").toString()<!>
const konst b2 = <!EVALUATED: `Person(name=Adam, phone=123)`!>Person("John", 789).copy("Adam", 123).toString()<!>

const konst c = <!EVALUATED: `true`!>Person("John", 123456).equals(Person("John", 123456))<!>
const konst d = <!EVALUATED: `Person name is John and his phone is 123456`!>Person("John", 123456).getAsString()<!>

@CompileTimeCalculation
data class WithArray(konst array: Array<*>?, konst intArray: IntArray?)

const konst e1 = <!EVALUATED: `WithArray(array=[1, 2.0], intArray=[1, 2, 3])`!>WithArray(arrayOf(1, 2.0), intArrayOf(1, 2, 3)).toString()<!>
const konst e2 = <!EVALUATED: `WithArray(array=null, intArray=[1, 2, 3])`!>WithArray(null, intArrayOf(1, 2, 3)).toString()<!>
const konst e3 = <!EVALUATED: `WithArray(array=[1, false], intArray=null)`!>WithArray(arrayOf("1", false), null).toString()<!>
const konst e4 = <!EVALUATED: `WithArray(array=null, intArray=null)`!>WithArray(null, null).toString()<!>
