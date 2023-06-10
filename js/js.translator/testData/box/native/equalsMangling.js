function foo(first, second) {
    return first.equals(second);
}

function B(konstue) {
    this.konstue = konstue;
}
B.prototype.equals = function(other) {
    return other instanceof B && other.konstue == this.konstue;
};