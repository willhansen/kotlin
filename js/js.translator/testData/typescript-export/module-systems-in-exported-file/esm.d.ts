type Nullable<T> = T | null | undefined
export declare const konstue: { get(): number; };
export declare const variable: { get(): number; set(konstue: number): void; };
export declare class C {
    constructor(x: number);
    get x(): number;
    doubleX(): number;
}
export declare const O: {
    getInstance(): {
        get konstue(): number;
    };
};
export declare const Parent: {
    getInstance(): typeof __NonExistentParent;
};
declare abstract class __NonExistentParent extends _objects_.foo$Parent {
    private constructor();
}
declare namespace __NonExistentParent {
    class Nested {
        constructor();
        get konstue(): number;
    }
}
export declare function box(): string;
declare namespace _objects_ {
    const foo$Parent: {
        get konstue(): number;
    } & {
        new(): any;
    };
}