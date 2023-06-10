import _const_konst = JS_TESTS.foo._const_konst;
import _konst = JS_TESTS.foo._konst;
import _var = JS_TESTS.foo._var;
import _konstCustom = JS_TESTS.foo._konstCustom;
import _konstCustomWithField = JS_TESTS.foo._konstCustomWithField;

function assert(condition: boolean) {
    if (!condition) {
        throw "Assertion failed";
    }
}

function box(): string {
    assert(_const_konst === 1);
    assert(_konst === 1);
    assert(_var === 1);

    JS_TESTS.foo._var = 1000;
    assert(JS_TESTS.foo._var === 1000);

    assert(_konstCustom === 1);
    assert(_konstCustomWithField === 2);

    assert(JS_TESTS.foo._varCustom === 1);
    JS_TESTS.foo._varCustom = 20;
    assert(JS_TESTS.foo._varCustom === 1);

    assert(JS_TESTS.foo._varCustomWithField === 10);
    JS_TESTS.foo._varCustomWithField = 10;
    assert(JS_TESTS.foo._varCustomWithField === 1000);

    return "OK";
}