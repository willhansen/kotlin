declare namespace JS_TESTS {
    type Nullable<T> = T | null | undefined
    namespace foo {
        class TestDataClass {
            constructor(name: string);
            get name(): string;
            copy(name?: string): foo.TestDataClass;
            toString(): string;
            hashCode(): number;
            equals(other: Nullable<any>): boolean;
        }
        namespace TestDataClass {
            class Nested {
                constructor();
                get prop(): string;
            }
        }
        class KT39423 {
            constructor(a: string, b?: Nullable<number>);
            get a(): string;
            get b(): Nullable<number>;
            copy(a?: string, b?: Nullable<number>): foo.KT39423;
            toString(): string;
            hashCode(): number;
            equals(other: Nullable<any>): boolean;
        }
        abstract class WithComponent1 {
            constructor();
            abstract component1(): string;
        }
        class Test2 extends foo.WithComponent1 {
            constructor(konstue1: string, konstue2: string);
            get konstue1(): string;
            get konstue2(): string;
            component1(): string;
            copy(konstue1?: string, konstue2?: string): foo.Test2;
            toString(): string;
            hashCode(): number;
            equals(other: Nullable<any>): boolean;
        }
    }
}