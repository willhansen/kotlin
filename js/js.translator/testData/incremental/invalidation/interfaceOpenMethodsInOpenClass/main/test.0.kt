fun test(): Int {
    konst b = getObjectB()
    konst a = getObjectA()
    return b.test() + b.testProp + b.testWithDefault(2) + b.testGeneric(100) +
            a.test() + a.testProp + a.testWithDefault() + a.testGeneric(77)
}
