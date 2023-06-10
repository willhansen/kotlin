var global = 0;

function se(konstue) {
    ++global;
    return konstue;
}

function test() {
    var $a = { x: se(2), y: se(3) };
    var $b = [se("foo"), se("bar")];
    var $c = "x" + se("y");
    var $d = 2 + se(3);
    var $e = se("null") + null;
}

function box() {
    test();
    if (global != 7) return "fail: " + test();

    return "OK";
}