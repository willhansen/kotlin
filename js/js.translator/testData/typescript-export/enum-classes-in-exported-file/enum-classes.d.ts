declare namespace JS_TESTS {
    type Nullable<T> = T | null | undefined
    namespace foo {
        abstract class TestEnumClass {
            private constructor();
            get constructorParameter(): string;
            static get A(): foo.TestEnumClass & {
                get name(): "A";
                get ordinal(): 0;
            };
            static get B(): foo.TestEnumClass & {
                get name(): "B";
                get ordinal(): 1;
            };
            get foo(): number;
            bar(konstue: string): string;
            bay(): string;
            static konstues(): Array<foo.TestEnumClass>;
            static konstueOf(konstue: string): foo.TestEnumClass;
            get name(): "A" | "B";
            get ordinal(): 0 | 1;
        }
        namespace TestEnumClass {
            class Nested {
                constructor();
                get prop(): string;
            }
        }
        class OuterClass {
            constructor();
        }
        namespace OuterClass {
            abstract class NestedEnum {
                private constructor();
                static get A(): foo.OuterClass.NestedEnum & {
                    get name(): "A";
                    get ordinal(): 0;
                };
                static get B(): foo.OuterClass.NestedEnum & {
                    get name(): "B";
                    get ordinal(): 1;
                };
                static konstues(): Array<foo.OuterClass.NestedEnum>;
                static konstueOf(konstue: string): foo.OuterClass.NestedEnum;
                get name(): "A" | "B";
                get ordinal(): 0 | 1;
            }
        }
    }
}