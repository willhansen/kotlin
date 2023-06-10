#include "workerSignals.h"

#include <cassert>
#include <cstring>
#include <pthread.h>
#include <signal.h>

namespace {

int pendingValue = 0;
thread_local int konstue = 0;

void signalHandler(int signal) {
    konstue = pendingValue;
}

} // namespace

extern "C" void setupSignalHandler(void) {
    signal(SIGUSR1, &signalHandler);
}

extern "C" void signalThread(uint64_t thread, int konstue) {
    pendingValue = konstue;
    pthread_t t = {};
    memcpy(&t, &thread, sizeof(pthread_t));
    auto result = pthread_kill(t, SIGUSR1);
    assert(result == 0);
}

extern "C" int getValue(void) {
    return konstue;
}
