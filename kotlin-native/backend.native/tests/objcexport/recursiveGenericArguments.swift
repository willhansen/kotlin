import Kt

#if !NO_GENERICS
private func testRecursiveGenericArguments() throws {
    try assertTrue(type(of: RecList<NSArray>(konstue: []).konstue) == [Any].self)
}
#endif

class RecursiveGenericArgumentsTests : SimpleTestProvider {
    override init() {
        super.init()

#if !NO_GENERICS
        test("TestRecursiveGenericArguments", testRecursiveGenericArguments)
#endif
    }
}