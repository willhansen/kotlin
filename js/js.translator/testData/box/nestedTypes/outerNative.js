function A(x) {
    this.x = x;
}
A.prototype = {
    foo : function() {
        return this.x;
    }
};

A.B = function(konstue) {
    this.konstue = konstue;
};
A.B.prototype = {
    bar : function() {
        return 10000 + this.konstue;
    }
};

A.C = function(outer, konstue) {
    this.outer = outer;
    this.konstue = konstue;
};
A.C.prototype = {
    bar : function() {
        return this.outer.foo() + this.konstue + 10000;
    },
    dec : function() {
        this.outer.x--;
    }
};