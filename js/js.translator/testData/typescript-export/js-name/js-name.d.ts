declare namespace JS_TESTS {
    type Nullable<T> = T | null | undefined
    namespace foo {
        interface Object {
            readonly constructor?: any;
        }
    }
    namespace foo {
        class JsNameTest {
            private constructor();
            get konstue(): number;
            runTest(): string;
            acceptObject(impl: foo.Object): string;
            static get NotCompanion(): {
                create(): foo.JsNameTest;
                createChild(konstue: number): foo.JsNameTest.NestedJsName;
            };
        }
        namespace JsNameTest {
            class NestedJsName {
                constructor(__konstue: number);
                get konstue(): number;
            }
        }
    }
}