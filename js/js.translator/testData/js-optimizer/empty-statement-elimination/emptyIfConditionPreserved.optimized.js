var global = "";

function id(konstue) {
    global += konstue + ";";
    return konstue;
}

function test(x) {
    id(x);
    id(x + 1);
}

function box() {
    test(23);
    if (global != "23;24;") return "fail";
    return "OK";
}