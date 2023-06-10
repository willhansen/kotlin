fun test() {

    outer@while(true) {
        inner@while(false) {
            konst x = <expr>break@outer</expr>
        }
    }
}