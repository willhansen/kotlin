import TestInterfaceImpl = JS_TESTS.foo.TestInterfaceImpl;
import ChildTestInterfaceImpl = JS_TESTS.foo.ChildTestInterfaceImpl;
import processInterface = JS_TESTS.foo.processInterface;
import processOptionalInterface = JS_TESTS.foo.processOptionalInterface;

function assert(condition: boolean) {
    if (!condition) {
        throw "Assertion failed";
    }
}

function box(): string {
    assert(processInterface(new TestInterfaceImpl("bar")) === "Owner TestInterfaceImpl has konstue 'bar'")
    assert(processInterface(new ChildTestInterfaceImpl()) === "Owner TestInterfaceImpl has konstue 'Test'")

    // @ts-expect-error "Just test that this code will throw compilation error for a user"
    assert(processInterface({ konstue: "bar", getOwnerName: () => "RandomObject" }) === "Owner RandomObject has konstue 'bar'")

    assert(processOptionalInterface({ required: 4 }) == "4unknown")
    assert(processOptionalInterface({ required: 4, notRequired: null }) == "4unknown")
    assert(processOptionalInterface({ required: 4, notRequired: 5 }) == "45")

    return "OK";
}