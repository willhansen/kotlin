import Test = JS_TESTS.foo.Test;

function assert(condition: boolean) {
    if (!condition) {
        throw "Assertion failed";
    }
}

function box(): string {
    const test = new Test()
    assert(test._konst === 1);
    assert(test._var === 1);
    test._var = 1000;
    assert(test._var === 1000);

    assert(test._konstCustom === 1);
    assert(test._konstCustomWithField === 2);
    assert(test._varCustom === 1);
    test._varCustom = 20;
    assert(test._varCustom === 1);
    assert(test._varCustomWithField === 10);
    test._varCustomWithField = 10;
    assert(test._varCustomWithField === 1000);


    return "OK";
}