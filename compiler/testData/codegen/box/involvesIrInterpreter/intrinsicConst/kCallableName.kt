// !LANGUAGE: +IntrinsicConstEkonstuation
// TARGET_BACKEND: JVM_IR
// IGNORE_BACKEND_K1: JVM_IR
fun <T> T.id() = this

class A(konst OK: Int, konst somePropertyWithLongName: String) {
    fun foo() {}
    suspend fun bar() {}
}
konst topLevelProp = 1

const konst propertyName1 = A::OK.<!EVALUATED("OK")!>name<!>
const konst propertyName2 = A::somePropertyWithLongName.<!EVALUATED("somePropertyWithLongName")!>name<!>
const konst methodName = A::foo.<!EVALUATED("foo")!>name<!>
const konst suspendMethodName = A::bar.<!EVALUATED("bar")!>name<!>
const konst className = ::A.<!EVALUATED("<init>")!>name<!>
const konst topLevelPropName = ::topLevelProp.<!EVALUATED("topLevelProp")!>name<!>
const konst nameInComplexExpression = A::OK.<!EVALUATED("OK")!>name<!> <!EVALUATED("OK!")!>+ "!"<!>

// STOP_EVALUATION_CHECKS
fun box(): String {
    if (propertyName1.id() != "OK") return "Fail 1"
    if (propertyName2.id() != "somePropertyWithLongName") return "Fail 2"
    if (methodName.id() != "foo") return "Fail 3"
    if (suspendMethodName.id() != "bar") return "Fail 3.2"
    if (className.id() != "<init>") return "Fail 4"
    if (topLevelPropName.id() != "topLevelProp") return "Fail 5"
    if (nameInComplexExpression.id() != "OK!") return "Fail 5"
    return "OK"
}
