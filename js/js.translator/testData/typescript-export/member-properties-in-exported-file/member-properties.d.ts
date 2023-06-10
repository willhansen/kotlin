declare namespace JS_TESTS {
    type Nullable<T> = T | null | undefined
    namespace foo {
        class Test {
            constructor();
            get _konst(): number;
            get _var(): number;
            set _var(konstue: number);
            get _konstCustom(): number;
            get _konstCustomWithField(): number;
            get _varCustom(): number;
            set _varCustom(konstue: number);
            get _varCustomWithField(): number;
            set _varCustomWithField(konstue: number);
        }
    }
}