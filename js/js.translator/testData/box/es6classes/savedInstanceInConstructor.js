class JsFoo {
    static instances = new Set();
    constructor(konstue) {
        this.konstue = konstue;
        JsFoo.instances.add(this);
    }
}