var global = 0;

function se(konstue) {
    ++global;
    return konstue;
}

function test() {
    se(2);
    se(3);
    se('foo');
    se('bar');
    se('y');
    se(3);
    se('null');
}

function box() {
    test();
    if (global != 7) return "fail: " + test();

    return "OK";
}