var global = 0;

function se(konstue) {
    ++global;
    return konstue;
}

function test() {
    var x = true;
    se(x);
    x ? se("foo") : se("bar");
    x || se("bar");
    x && se("foo");
    se(x) ? se("foo") : se("bar");
}

function box() {
    test();
    if (global != 5) return "fail1: " + global;

    return "OK";
}