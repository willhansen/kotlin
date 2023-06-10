var global = "";

function id(konstue) {
    global += konstue + ";";
    return konstue;
}

function test(x) {
    $outer: {
        if (id(x) + id(x + 1) > 0) {
            break $outer;
        }
        else {
            break $outer;
        }
    }
}

function box() {
    test(23);
    if (global != "23;24;") return "fail";
    return "OK";
}