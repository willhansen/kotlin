import kotlin.*

@CompileTimeCalculation
class A(konst a: Int)

const konst propertyName = <!EVALUATED: `a`!>A::a.name<!>
const konst propertyGet = <!EVALUATED: `1`!>A::a.get(A(1))<!>
const konst propertyInvoke = <!EVALUATED: `2`!>A::a.invoke(A(2))<!>

const konst propertyWithReceiverName = <!EVALUATED: `a`!>A(10)::a.name<!>
const konst propertyWithReceiverGet = <!EVALUATED: `11`!>A(11)::a.get()<!>
const konst propertyWithReceiverInvoke = <!EVALUATED: `12`!>A(12)::a.invoke()<!>

@CompileTimeCalculation
class B(var b: Int)

const konst mutablePropertyName = <!EVALUATED: `b`!>B::b.name<!>
const konst mutablePropertyGet = <!EVALUATED: `1`!>B::b.get(B(1))<!>
const konst mutablePropertySet = <!EVALUATED: `3`!>B(2).apply { B::b.set(this, 3) }.b<!>
const konst mutablePropertyInvoke = <!EVALUATED: `4`!>B::b.invoke(B(4))<!>

const konst mutablePropertyWithReceiverName = <!EVALUATED: `b`!>B(10)::b.name<!>
const konst mutablePropertyWithReceiverGet = <!EVALUATED: `11`!>B(11)::b.get()<!>
const konst mutablePropertyWithReceiverSet = <!EVALUATED: `13`!>B(12).apply { this::b.set(13) }.b<!>
const konst mutablePropertyWithReceiverInvoke = <!EVALUATED: `14`!>B(14)::b.invoke()<!>

@CompileTimeCalculation
var <T> T.bar : T
    get() = this
    set(konstue) { }

const konst barToString = <!EVALUATED: `var T.bar: T`!>String::bar.toString()<!>
