annotation class Ann

interface IFoo {
    @Ann konst testVal: String
    @Ann fun testFun()
    @Ann konst String.testExtVal: String
    @Ann fun String.testExtFun()
}

class DFoo(d: IFoo) : IFoo by d
