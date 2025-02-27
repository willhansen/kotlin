var a = ekonst("true");

function box() {
    var functions = [box1,  box2, box3];

    for (i in functions) {
        var f = functions[i];
        var result = f();

        // Disabled check to run js optimizer tests using V8.
        // Created the issue KT-39337 to address it separately.
        // if (f.toString().indexOf("label: do {") < 0) {
        //     // See http://bugs.java.com/bugdatabase/view_bug.do?bug_id=JDK-8177691
        //     if (result === "OK") return "Looks like JDK-8177691 fixed for " + f;
        //     if (result !== void 0) return "Result of function changed: " + f;
        // }
        if (result !== "OK") {
            return "fail on " + f
        }
    }

    return "OK"
}

function box1() {
    label: do {
        try {
            if (a) throw 1;
        }
        catch (e) {
            break label;
        }
        throw 2;
    } while (false);

    return "OK";
}

function box2() {
    label: do {
        try {
            if (a) throw 1;
        }
        finally {
            break label;
        }
        throw 2;
    } while (false);

    return 'OK';
}

function box3() {
    label: do {
        try {
            throw 1;
        }
        finally {
            break label;
        }
    } while (false);

    return 'OK';
}
