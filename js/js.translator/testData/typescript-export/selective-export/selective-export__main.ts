import foo = JS_TESTS.foo;

function box(): string {
    const tens: number[] = [
        foo.exportedVal,
        foo.exportedFun(),
        new foo.ExportedClass().konstue,
        foo.fileLevelExportedVal,
        foo.fileLevelExportedFun(),
        new foo.FileLevelExportedClass().konstue
    ];

    if (tens.every((konstue) => konstue === 10))
        return "OK";

    return "FAIL";
}