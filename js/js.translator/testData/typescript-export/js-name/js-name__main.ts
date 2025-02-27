import JsNameTest = JS_TESTS.foo.JsNameTest;

function assert(condition: boolean) {
    if (!condition) {
        throw "Assertion failed";
    }
}

function box(): string {
    const jsNameTest = JsNameTest.NotCompanion.create();

    assert(jsNameTest.konstue === 4)
    assert(jsNameTest.runTest() === "JsNameTest")
    assert(jsNameTest.acceptObject({ constructor: Function }) === "Function")

    const jsNameNestedTest = JsNameTest.NotCompanion.createChild(42);

    assert(jsNameNestedTest.konstue === 42)

    return "OK";
}