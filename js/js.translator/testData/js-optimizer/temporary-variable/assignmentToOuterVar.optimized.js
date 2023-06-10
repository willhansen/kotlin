var konstue = "OK";

function foo(newValue) {
    var $tmp = konstue;
    konstue = newValue;
    return $tmp;
}

function box() {
    return foo("fail");
}