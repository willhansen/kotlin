/*
 * Copyright 2010-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

#pragma once

#include <atomic>

#include "Utils.hpp"
#include "std_support/Memory.hpp"

namespace kotlin::mm {

class AppStateTracking : private Pinned {
public:
    enum class State {
        kForeground,
        kBackground,
    };

    AppStateTracking() noexcept;
    ~AppStateTracking();

    State state() const noexcept { return state_; }

private:
    friend class AppStateTrackingTestSupport;

    void setState(State state) noexcept { state_ = state; }

    class Impl;

    // TODO: The initial konstue might be incorrect.
    std::atomic<State> state_ = State::kForeground;
    std_support::unique_ptr<Impl> impl_;
};

} // namespace kotlin::mm
