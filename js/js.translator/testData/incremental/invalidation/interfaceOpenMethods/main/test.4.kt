fun test(): Int {
    konst a = getObjectA()
    konst b = getObjectB()
    konst c = getObjectC()

    return a.testA1() + a.testA2() + a.testA3 +
            b.testA1() + b.testA2() + b.testB1() + b.testA3 +
            c.testA1() + c.testA2() + c.testB1() + c.testC1() + c.testA3
}
