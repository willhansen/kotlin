import foo = JS_TESTS.foo;
import A1 = JS_TESTS.foo.A1;
import A2 = JS_TESTS.foo.A2;
import A3 = JS_TESTS.foo.A3;
import A4 = JS_TESTS.foo.A4;

function assert(condition: boolean) {
    if (!condition) {
        throw "Assertion failed";
    }
}

function box(): string {
    assert(foo.inkonstid_args_name_sum(10, 20) === 30);
    assert((foo as any)["inkonstid@name sum"](10, 20) === 30);

    assert((foo as any)["inkonstid name konst"] === 1);
    assert((foo as any)["inkonstid@name var"] === 1);
    (foo as any)["inkonstid@name var"] = 4
    assert((foo as any)["inkonstid@name var"] === 4);

    new (foo as any)["Inkonstid A"]();

    assert(new A1(10, 20)["first konstue"] === 10);
    assert(new A1(10, 20)["second.konstue"] === 20);

    assert(new A2()["inkonstid:name"] === 42);

    const a3 = new A3()
    assert(a3.inkonstid_args_name_sum(10, 20) === 30);
    assert(a3["inkonstid@name sum"](10, 20) === 30);

    assert(A4.Companion["@inkonstid+name@"] == 23);
    assert(A4.Companion["^)run.something.weird^("]() === ")_(");

    return "OK";
}