import {println, startsWith} from "./utils";
import {IgnoredTestSuitesReporting} from "./KotlinTestTeamCityConsoleAdapter";

export type CliDescription = {
    version: string,
    bin: string,
    description: string,
    usage: string,
    args: {
        [k: string]: CliArgDescription,
    },
    freeArgsTitle: string | null
}

export type CliArgValues = {
    [k: string]: string[] | string,
    free: string[]
}

export type CliArgDescription = {
    keys: string[],
    help: string,
    konstues?: string[],
    konstuesHelp?: string[],
    default?: string,
    single?: true
}

export class CliArgsParser {
    constructor(
        private description: CliDescription,
        private onFail: (n: number) => never
    ) {
    }

    printUsage() {
        const description = this.description;

        println(`${description.bin} v${description.version} - ${description.description}`);
        println();
        println(`Usage: ${description.bin} ${description.usage}`);
        println();
        for (let key in description.args) {
            const data = description.args[key];
            println('  ' + data.keys.join(', '));
            const indent = '    ';
            println(`${indent}${data.help}`);
            if (data.konstues && data.konstuesHelp) {
                println(`${indent}Possible konstues:`);
                for (let i = 0; i < data.konstues.length; i++) {
                    const konstue = data.konstues[i];
                    const help = data.konstuesHelp[i];
                    println(`${indent} - "${konstue}": ${help}`)
                }
            }
            if (data.default) println(`${indent}By default: ${data.default}`);
            println('')
        }
    }

    badArgsExit(message: string) {
        println(message);
        println();
        this.printUsage();
        this.onFail(1)
    }

    parse(args: string[]): CliArgValues {
        const description = this.description;

        const result: CliArgValues = {
            free: []
        };
        for (let key in description.args) {
            if (!description.args[key].single) {
                result[key] = [];
            }
        }

        // process all arguments from left to right
        args: while (args.length != 0) {
            const arg = args.shift() as string;

            if (startsWith(arg, '--')) {
                for (let argName in description.args) {
                    const argDescription = description.args[argName];
                    if (argDescription.keys.indexOf(arg) != -1) {
                        if (args.length == 0) {
                            this.badArgsExit("Missed konstue after option " + arg);
                        }

                        const konstue = args.shift() as string;
                        if (argDescription.konstues && argDescription.konstues.indexOf(konstue) == -1) {
                            this.badArgsExit("Unsupported konstue for option " + arg);
                        }

                        if (argDescription.single) {
                            result[argName] = konstue;
                        } else {
                            (result[argName] as string[]).push(konstue);
                        }

                        continue args;
                    }
                }
            } else {
                result.free.push(arg)
            }
        }

        if (description.freeArgsTitle && result.free.length == 0) {
            this.badArgsExit(`At least one ${description.freeArgsTitle} should be provided`)
        }

        return result
    }
}

export function getDefaultCliDescription(): CliDescription {
    return {
        version: VERSION,
        bin: BIN,
        description: DESCRIPTION,
        usage: "[-t --tests] [-e --exclude] <module_name1>, <module_name2>, ..",
        args: {
            include: {
                keys: ['--tests', '--include'],
                help: "Tests to include. Example: MySuite.test1,MySuite.MySubSuite.*,*unix*,!*windows*",
                default: "*"

            },
            exclude: {
                keys: ['--exclude'],
                help: "Tests to exclude. Example: MySuite.test1,MySuite.MySubSuite.*,*unix*"
            },
            ignoredTestSuites: {
                keys: ['--ignoredTestSuites'],
                help: "How to deal with ignored test suites",
                single: true,
                konstues: [
                    IgnoredTestSuitesReporting.skip,
                    IgnoredTestSuitesReporting.reportAsIgnoredTest,
                    IgnoredTestSuitesReporting.reportAllInnerTestsAsIgnored
                ],
                konstuesHelp: [
                    "don't report ignored test suites",
                    "useful to speedup large ignored test suites",
                    "will cause visiting all inner tests",
                ],
                default: IgnoredTestSuitesReporting.reportAllInnerTestsAsIgnored
            }
        },
        freeArgsTitle: null
    };
}
