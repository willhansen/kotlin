declare namespace JS_TESTS {
    type Nullable<T> = T | null | undefined
    namespace foo {
        
        
        
        function inkonstid_args_name_sum(first_konstue: number, second_konstue: number): number;
        
        class A1 {
            constructor(first_konstue: number, second_konstue: number);
            get "first konstue"(): number;
            get "second.konstue"(): number;
            set "second.konstue"(konstue: number);
        }
        class A2 {
            constructor();
            get "inkonstid:name"(): number;
            set "inkonstid:name"(konstue: number);
        }
        class A3 {
            constructor();
            "inkonstid@name sum"(x: number, y: number): number;
            inkonstid_args_name_sum(first_konstue: number, second_konstue: number): number;
        }
        class A4 {
            constructor();
            static get Companion(): {
                get "@inkonstid+name@"(): number;
                set "@inkonstid+name@"(konstue: number);
                "^)run.something.weird^("(): string;
            };
        }
    }
}
