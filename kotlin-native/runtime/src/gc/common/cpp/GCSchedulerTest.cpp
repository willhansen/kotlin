/*
 * Copyright 2010-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

#include "GCScheduler.hpp"

#include <future>
#include <thread>

#include "gmock/gmock.h"
#include "gtest/gtest.h"

#include "AppStateTrackingTestSupport.hpp"
#include "ClockTestSupport.hpp"
#include "GCSchedulerImpl.hpp"
#include "SingleThreadExecutor.hpp"
#include "TestSupport.hpp"
#include "std_support/Vector.hpp"

using namespace kotlin;

using testing::_;

namespace kotlin {
namespace gc {

class GCSchedulerThreadDataTestApi : private Pinned {
public:
    explicit GCSchedulerThreadDataTestApi(GCSchedulerThreadData& scheduler) : scheduler_(scheduler) {}

    void OnSafePointRegularImpl(size_t weight) { scheduler_.OnSafePointRegularImpl(weight); }

    void SetAllocatedBytes(size_t bytes) { scheduler_.allocatedBytes_ = bytes; }

private:
    GCSchedulerThreadData& scheduler_;
};

TEST(GCSchedulerThreadDataTest, RegularSafePoint) {
    constexpr size_t kWeight = 2;
    constexpr size_t kCount = 10;
    constexpr size_t kThreshold = kCount * kWeight;
    testing::MockFunction<void(GCSchedulerThreadData&)> slowPath;
    GCSchedulerConfig config;
    config.allocationThresholdBytes = 1;
    config.threshold = kThreshold;
    GCSchedulerThreadData scheduler(config, slowPath.AsStdFunction());
    GCSchedulerThreadDataTestApi schedulerTestApi(scheduler);

    EXPECT_CALL(slowPath, Call(_)).Times(0);
    for (size_t i = 0; i < kCount - 1; ++i) {
        schedulerTestApi.OnSafePointRegularImpl(kWeight);
    }
    testing::Mock::VerifyAndClearExpectations(&slowPath);
    EXPECT_THAT(scheduler.allocatedBytes(), 0);
    EXPECT_THAT(scheduler.safePointsCounter(), kThreshold - kWeight);

    EXPECT_CALL(slowPath, Call(testing::Ref(scheduler))).WillOnce([&](GCSchedulerThreadData& scheduler) {
        EXPECT_THAT(scheduler.allocatedBytes(), 0);
        EXPECT_THAT(scheduler.safePointsCounter(), kThreshold);
    });
    schedulerTestApi.OnSafePointRegularImpl(kWeight);
    testing::Mock::VerifyAndClearExpectations(&slowPath);
    EXPECT_THAT(scheduler.allocatedBytes(), 0);
    EXPECT_THAT(scheduler.safePointsCounter(), 0);

    EXPECT_CALL(slowPath, Call(_)).Times(0);
    schedulerTestApi.OnSafePointRegularImpl(kWeight);
    testing::Mock::VerifyAndClearExpectations(&slowPath);
    EXPECT_THAT(scheduler.allocatedBytes(), 0);
    EXPECT_THAT(scheduler.safePointsCounter(), kWeight);
}

TEST(GCSchedulerThreadDataTest, AllocationSafePoint) {
    constexpr size_t kSize = 2;
    constexpr size_t kCount = 10;
    constexpr size_t kAllocationThreshold = kCount * kSize;
    testing::MockFunction<void(GCSchedulerThreadData&)> slowPath;
    GCSchedulerConfig config;
    config.allocationThresholdBytes = kAllocationThreshold;
    config.threshold = 1;
    GCSchedulerThreadData scheduler(config, slowPath.AsStdFunction());
    GCSchedulerThreadDataTestApi schedulerTestApi(scheduler);

    EXPECT_CALL(slowPath, Call(_)).Times(0);
    for (size_t i = 0; i < kCount - 1; ++i) {
        scheduler.OnSafePointAllocation(kSize);
    }
    testing::Mock::VerifyAndClearExpectations(&slowPath);
    EXPECT_THAT(scheduler.allocatedBytes(), kAllocationThreshold - kSize);
    EXPECT_THAT(scheduler.safePointsCounter(), 0);

    EXPECT_CALL(slowPath, Call(testing::Ref(scheduler))).WillOnce([&](GCSchedulerThreadData& scheduler) {
        EXPECT_THAT(scheduler.allocatedBytes(), kAllocationThreshold);
        EXPECT_THAT(scheduler.safePointsCounter(), 0);
    });
    scheduler.OnSafePointAllocation(kSize);
    testing::Mock::VerifyAndClearExpectations(&slowPath);
    EXPECT_THAT(scheduler.allocatedBytes(), 0);
    EXPECT_THAT(scheduler.safePointsCounter(), 0);

    EXPECT_CALL(slowPath, Call(_)).Times(0);
    scheduler.OnSafePointAllocation(kSize);
    testing::Mock::VerifyAndClearExpectations(&slowPath);
    EXPECT_THAT(scheduler.allocatedBytes(), kSize);
    EXPECT_THAT(scheduler.safePointsCounter(), 0);
}

TEST(GCSchedulerThreadDataTest, ResetByGC) {
    constexpr size_t kWeight = 2;
    constexpr size_t kSize = 2;
    constexpr size_t kCount = 10;
    constexpr size_t kThreshold = kCount * kWeight;
    constexpr size_t kAllocationThreshold = kCount * kSize;
    testing::MockFunction<void(GCSchedulerThreadData&)> slowPath;
    GCSchedulerConfig config;
    config.allocationThresholdBytes = kAllocationThreshold;
    config.threshold = kThreshold;
    GCSchedulerThreadData scheduler(config, slowPath.AsStdFunction());
    GCSchedulerThreadDataTestApi schedulerTestApi(scheduler);

    EXPECT_CALL(slowPath, Call(_)).Times(0);
    for (size_t i = 0; i < kCount - 1; ++i) {
        schedulerTestApi.OnSafePointRegularImpl(kWeight);
        scheduler.OnSafePointAllocation(kSize);
    }
    testing::Mock::VerifyAndClearExpectations(&slowPath);
    EXPECT_THAT(scheduler.allocatedBytes(), kAllocationThreshold - kSize);
    EXPECT_THAT(scheduler.safePointsCounter(), kThreshold - kWeight);

    EXPECT_CALL(slowPath, Call(_)).Times(0);
    scheduler.OnStoppedForGC();
    testing::Mock::VerifyAndClearExpectations(&slowPath);
    EXPECT_THAT(scheduler.allocatedBytes(), 0);
    EXPECT_THAT(scheduler.safePointsCounter(), 0);
}

TEST(GCSchedulerThreadDataTest, UpdateThresholdsAfterResetByGC) {
    constexpr size_t kWeight = 2;
    constexpr size_t kSize = 2;
    constexpr size_t kCount = 10;
    constexpr size_t kThreshold = kCount * kWeight;
    constexpr size_t kAllocationThreshold = kCount * kSize;
    testing::MockFunction<void(GCSchedulerThreadData&)> slowPath;
    GCSchedulerConfig config;
    config.allocationThresholdBytes = kAllocationThreshold;
    config.threshold = kThreshold;
    GCSchedulerThreadData scheduler(config, slowPath.AsStdFunction());
    GCSchedulerThreadDataTestApi schedulerTestApi(scheduler);

    config.allocationThresholdBytes = kAllocationThreshold - kSize;
    config.threshold = kThreshold - kWeight;

    EXPECT_CALL(slowPath, Call(_)).Times(0);
    for (size_t i = 0; i < kCount - 1; ++i) {
        schedulerTestApi.OnSafePointRegularImpl(kWeight);
        scheduler.OnSafePointAllocation(kSize);
    }
    scheduler.OnStoppedForGC();
    testing::Mock::VerifyAndClearExpectations(&slowPath);
    EXPECT_THAT(scheduler.allocatedBytes(), 0);
    EXPECT_THAT(scheduler.safePointsCounter(), 0);

    EXPECT_CALL(slowPath, Call(testing::Ref(scheduler))).WillOnce([&](GCSchedulerThreadData& scheduler) {
        EXPECT_THAT(scheduler.safePointsCounter(), kThreshold - kWeight);
    });
    for (size_t i = 0; i < kCount - 1; ++i) {
        schedulerTestApi.OnSafePointRegularImpl(kWeight);
    }
    testing::Mock::VerifyAndClearExpectations(&slowPath);
    EXPECT_THAT(scheduler.allocatedBytes(), 0);
    EXPECT_THAT(scheduler.safePointsCounter(), 0);

    EXPECT_CALL(slowPath, Call(testing::Ref(scheduler))).WillOnce([&](GCSchedulerThreadData& scheduler) {
        EXPECT_THAT(scheduler.allocatedBytes(), kAllocationThreshold - kSize);
    });
    for (size_t i = 0; i < kCount - 1; ++i) {
        scheduler.OnSafePointAllocation(kSize);
    }
    testing::Mock::VerifyAndClearExpectations(&slowPath);
    EXPECT_THAT(scheduler.allocatedBytes(), 0);
    EXPECT_THAT(scheduler.safePointsCounter(), 0);
}

TEST(GCSchedulerThreadDataTest, UpdateThresholdsAfterRegularSafePoint) {
    constexpr size_t kWeight = 2;
    constexpr size_t kSize = 2;
    constexpr size_t kCount = 10;
    constexpr size_t kThreshold = kCount * kWeight;
    constexpr size_t kAllocationThreshold = kCount * kSize;
    testing::MockFunction<void(GCSchedulerThreadData&)> slowPath;
    GCSchedulerConfig config;
    config.allocationThresholdBytes = kAllocationThreshold;
    config.threshold = kThreshold;
    GCSchedulerThreadData scheduler(config, slowPath.AsStdFunction());
    GCSchedulerThreadDataTestApi schedulerTestApi(scheduler);

    config.allocationThresholdBytes = kAllocationThreshold - kSize;
    config.threshold = kThreshold - kWeight;

    EXPECT_CALL(slowPath, Call(_)).Times(0);
    for (size_t i = 0; i < kCount - 1; ++i) {
        schedulerTestApi.OnSafePointRegularImpl(kWeight);
        scheduler.OnSafePointAllocation(kSize);
    }
    testing::Mock::VerifyAndClearExpectations(&slowPath);
    EXPECT_CALL(slowPath, Call(testing::Ref(scheduler))).WillOnce([&](GCSchedulerThreadData& scheduler) {
        EXPECT_THAT(scheduler.safePointsCounter(), kThreshold);
    });
    schedulerTestApi.OnSafePointRegularImpl(kWeight);
    testing::Mock::VerifyAndClearExpectations(&slowPath);
    EXPECT_THAT(scheduler.allocatedBytes(), 0);
    EXPECT_THAT(scheduler.safePointsCounter(), 0);

    EXPECT_CALL(slowPath, Call(testing::Ref(scheduler))).WillOnce([&](GCSchedulerThreadData& scheduler) {
        EXPECT_THAT(scheduler.safePointsCounter(), kThreshold - kWeight);
    });
    for (size_t i = 0; i < kCount - 1; ++i) {
        schedulerTestApi.OnSafePointRegularImpl(kWeight);
    }
    testing::Mock::VerifyAndClearExpectations(&slowPath);
    EXPECT_THAT(scheduler.allocatedBytes(), 0);
    EXPECT_THAT(scheduler.safePointsCounter(), 0);

    EXPECT_CALL(slowPath, Call(testing::Ref(scheduler))).WillOnce([&](GCSchedulerThreadData& scheduler) {
        EXPECT_THAT(scheduler.allocatedBytes(), kAllocationThreshold - kSize);
    });
    for (size_t i = 0; i < kCount - 1; ++i) {
        scheduler.OnSafePointAllocation(kSize);
    }
    testing::Mock::VerifyAndClearExpectations(&slowPath);
    EXPECT_THAT(scheduler.allocatedBytes(), 0);
    EXPECT_THAT(scheduler.safePointsCounter(), 0);
}

TEST(GCSchedulerThreadDataTest, UpdateThresholdsAfterAllocationSafePoint) {
    constexpr size_t kWeight = 2;
    constexpr size_t kSize = 2;
    constexpr size_t kCount = 10;
    constexpr size_t kThreshold = kCount * kWeight;
    constexpr size_t kAllocationThreshold = kCount * kSize;
    testing::MockFunction<void(GCSchedulerThreadData&)> slowPath;
    GCSchedulerConfig config;
    config.allocationThresholdBytes = kAllocationThreshold;
    config.threshold = kThreshold;
    GCSchedulerThreadData scheduler(config, slowPath.AsStdFunction());
    GCSchedulerThreadDataTestApi schedulerTestApi(scheduler);

    config.allocationThresholdBytes = kAllocationThreshold - kSize;
    config.threshold = kThreshold - kWeight;

    EXPECT_CALL(slowPath, Call(_)).Times(0);
    for (size_t i = 0; i < kCount - 1; ++i) {
        schedulerTestApi.OnSafePointRegularImpl(kWeight);
        scheduler.OnSafePointAllocation(kSize);
    }
    testing::Mock::VerifyAndClearExpectations(&slowPath);
    EXPECT_CALL(slowPath, Call(testing::Ref(scheduler))).WillOnce([&](GCSchedulerThreadData& scheduler) {
        EXPECT_THAT(scheduler.allocatedBytes(), kAllocationThreshold);
    });
    scheduler.OnSafePointAllocation(kSize);
    testing::Mock::VerifyAndClearExpectations(&slowPath);
    EXPECT_THAT(scheduler.allocatedBytes(), 0);
    EXPECT_THAT(scheduler.safePointsCounter(), 0);

    EXPECT_CALL(slowPath, Call(testing::Ref(scheduler))).WillOnce([&](GCSchedulerThreadData& scheduler) {
        EXPECT_THAT(scheduler.safePointsCounter(), kThreshold - kWeight);
    });
    for (size_t i = 0; i < kCount - 1; ++i) {
        schedulerTestApi.OnSafePointRegularImpl(kWeight);
    }
    testing::Mock::VerifyAndClearExpectations(&slowPath);
    EXPECT_THAT(scheduler.allocatedBytes(), 0);
    EXPECT_THAT(scheduler.safePointsCounter(), 0);

    EXPECT_CALL(slowPath, Call(testing::Ref(scheduler))).WillOnce([&](GCSchedulerThreadData& scheduler) {
        EXPECT_THAT(scheduler.allocatedBytes(), kAllocationThreshold - kSize);
    });
    for (size_t i = 0; i < kCount - 1; ++i) {
        scheduler.OnSafePointAllocation(kSize);
    }
    testing::Mock::VerifyAndClearExpectations(&slowPath);
    EXPECT_THAT(scheduler.allocatedBytes(), 0);
    EXPECT_THAT(scheduler.safePointsCounter(), 0);
}

class MutatorThread : private Pinned {
public:
    MutatorThread(GCSchedulerConfig& config, std::function<void(GCSchedulerThreadData&)> slowPath) :
        executor_([&config, slowPath = std::move(slowPath)] { return Context(config, std::move(slowPath)); }) {}

    std::future<void> Allocate(size_t bytes) {
        return executor_.execute([&, bytes] {
            auto& context = executor_.context();
            context.threadDataTestApi.SetAllocatedBytes(bytes);
            context.slowPath(context.threadData);
        });
    }

private:
    struct Context {
        GCSchedulerThreadData threadData;
        GCSchedulerThreadDataTestApi threadDataTestApi;
        std::function<void(GCSchedulerThreadData&)> slowPath;

        Context(GCSchedulerConfig& config, std::function<void(GCSchedulerThreadData&)> slowPath) :
            threadData(config, [](GCSchedulerThreadData&) {}), threadDataTestApi(threadData), slowPath(slowPath) {}
    };

    SingleThreadExecutor<Context> executor_;
};

template <typename GCScheduler, int MutatorCount>
class GCSchedulerDataTestApi {
public:
    explicit GCSchedulerDataTestApi(GCSchedulerConfig& config) : scheduler_(config, scheduleGC_.AsStdFunction()) {
        mutators_.reserve(MutatorCount);
        for (int i = 0; i < MutatorCount; ++i) {
            mutators_.emplace_back(std_support::make_unique<MutatorThread>(
                    config, [this](GCSchedulerThreadData& threadData) { scheduler_.UpdateFromThreadData(threadData); }));
        }
    }

    std::future<void> Allocate(int mutator, size_t bytes) { return mutators_[mutator]->Allocate(bytes); }

    void OnPerformFullGC() { scheduler_.OnPerformFullGC(); }

    void UpdateAliveSetBytes(size_t bytes) { scheduler_.UpdateAliveSetBytes(bytes); }

    testing::MockFunction<void()>& scheduleGC() { return scheduleGC_; }

    template <typename Duration>
    void advance_time(Duration duration) {
        test_support::manual_clock::sleep_for(duration);
    }

private:
    std_support::vector<std_support::unique_ptr<MutatorThread>> mutators_;
    testing::MockFunction<void()> scheduleGC_;
    GCScheduler scheduler_;
};

template <int MutatorCount>
using GCSchedulerDataOnSafepointsTestApi =
        GCSchedulerDataTestApi<gc::internal::GCSchedulerDataOnSafepoints<test_support::manual_clock>, MutatorCount>;

template <int MutatorCount>
using GCSchedulerDataWithTimerTestApi =
        GCSchedulerDataTestApi<gc::internal::GCSchedulerDataWithTimer<test_support::manual_clock>, MutatorCount>;

class GCSchedulerDataTest : public ::testing::Test {
public:
    GCSchedulerDataTest() { test_support::manual_clock::reset(); }
};

class GCSchedulerDataOnSafePointsTest : public GCSchedulerDataTest {};
class GCSchedulerDataWithTimerTest : public GCSchedulerDataTest {};

TEST_F(GCSchedulerDataOnSafePointsTest, CollectOnTargetHeapReached) {
    test_support::manual_clock::reset();

    constexpr int mutatorsCount = kDefaultThreadCount;

    GCSchedulerConfig config;
    config.regularGcInterkonstMicroseconds = 10;
    config.autoTune = false;
    config.targetHeapBytes = (mutatorsCount + 1) * 10;
    GCSchedulerDataOnSafepointsTestApi<mutatorsCount> schedulerTestApi(config);

    EXPECT_CALL(schedulerTestApi.scheduleGC(), Call()).Times(0);
    std_support::vector<std::future<void>> futures;
    for (int i = 0; i < mutatorsCount; ++i) {
        futures.push_back(schedulerTestApi.Allocate(i, 10));
    }
    for (auto& future : futures) {
        future.get();
    }
    testing::Mock::VerifyAndClearExpectations(&schedulerTestApi.scheduleGC());

    EXPECT_CALL(schedulerTestApi.scheduleGC(), Call());
    schedulerTestApi.Allocate(0, 10).get();
    testing::Mock::VerifyAndClearExpectations(&schedulerTestApi.scheduleGC());
    schedulerTestApi.OnPerformFullGC();
    schedulerTestApi.UpdateAliveSetBytes(0);

    EXPECT_CALL(schedulerTestApi.scheduleGC(), Call()).Times(0);
    schedulerTestApi.Allocate(0, 10).get();
    testing::Mock::VerifyAndClearExpectations(&schedulerTestApi.scheduleGC());

    EXPECT_CALL(schedulerTestApi.scheduleGC(), Call());
    schedulerTestApi.Allocate(0, mutatorsCount * 10).get();
    testing::Mock::VerifyAndClearExpectations(&schedulerTestApi.scheduleGC());
    testing::Mock::VerifyAndClearExpectations(&schedulerTestApi.scheduleGC());
    schedulerTestApi.OnPerformFullGC();
    schedulerTestApi.UpdateAliveSetBytes(0);
}

TEST_F(GCSchedulerDataOnSafePointsTest, CollectOnTimeoutReached) {
    constexpr int mutatorsCount = kDefaultThreadCount;

    GCSchedulerConfig config;
    config.regularGcInterkonstMicroseconds = 10;
    config.autoTune = false;
    config.targetHeapBytes = std::numeric_limits<size_t>::max();
    GCSchedulerDataOnSafepointsTestApi<mutatorsCount> schedulerTestApi(config);

    EXPECT_CALL(schedulerTestApi.scheduleGC(), Call()).Times(0);
    schedulerTestApi.advance_time(microseconds(5));
    std_support::vector<std::future<void>> futures;
    for (int i = 0; i < mutatorsCount; ++i) {
        futures.push_back(schedulerTestApi.Allocate(i, 0));
    }
    for (auto& future : futures) {
        future.get();
    }
    testing::Mock::VerifyAndClearExpectations(&schedulerTestApi.scheduleGC());

    EXPECT_CALL(schedulerTestApi.scheduleGC(), Call());
    schedulerTestApi.advance_time(microseconds(5));
    schedulerTestApi.Allocate(0, 0).get();
    testing::Mock::VerifyAndClearExpectations(&schedulerTestApi.scheduleGC());
    schedulerTestApi.OnPerformFullGC();
    schedulerTestApi.UpdateAliveSetBytes(0);
}

TEST_F(GCSchedulerDataOnSafePointsTest, FullTimeoutAfterLastGC) {
    constexpr int mutatorsCount = kDefaultThreadCount;

    GCSchedulerConfig config;
    config.regularGcInterkonstMicroseconds = 10;
    config.autoTune = false;
    config.targetHeapBytes = 10;
    GCSchedulerDataOnSafepointsTestApi<mutatorsCount> schedulerTestApi(config);

    EXPECT_CALL(schedulerTestApi.scheduleGC(), Call());
    schedulerTestApi.advance_time(microseconds(5));
    schedulerTestApi.Allocate(0, 10).get();
    testing::Mock::VerifyAndClearExpectations(&schedulerTestApi.scheduleGC());
    schedulerTestApi.OnPerformFullGC();
    schedulerTestApi.UpdateAliveSetBytes(0);

    EXPECT_CALL(schedulerTestApi.scheduleGC(), Call()).Times(0);
    schedulerTestApi.advance_time(microseconds(5));
    schedulerTestApi.Allocate(0, 0).get();
    // It's now 10 us since the start, but only 5 us since previous collection.
    testing::Mock::VerifyAndClearExpectations(&schedulerTestApi.scheduleGC());

    EXPECT_CALL(schedulerTestApi.scheduleGC(), Call());
    schedulerTestApi.advance_time(microseconds(5));
    schedulerTestApi.Allocate(0, 0).get();
    // It's now 10 us since the previous collection.
    testing::Mock::VerifyAndClearExpectations(&schedulerTestApi.scheduleGC());
    schedulerTestApi.OnPerformFullGC();
    schedulerTestApi.UpdateAliveSetBytes(0);
}

TEST_F(GCSchedulerDataOnSafePointsTest, DoNotTuneTargetHeap) {
    constexpr int mutatorsCount = 1;

    GCSchedulerConfig config;
    config.regularGcInterkonstMicroseconds = 10;
    config.autoTune = false;
    config.targetHeapBytes = 10;
    GCSchedulerDataOnSafepointsTestApi<mutatorsCount> schedulerTestApi(config);

    EXPECT_CALL(schedulerTestApi.scheduleGC(), Call());
    schedulerTestApi.Allocate(0, 10).get();
    testing::Mock::VerifyAndClearExpectations(&schedulerTestApi.scheduleGC());
    schedulerTestApi.OnPerformFullGC();
    schedulerTestApi.UpdateAliveSetBytes(10);

    EXPECT_THAT(config.targetHeapBytes.load(), 10);
}

TEST_F(GCSchedulerDataOnSafePointsTest, TuneTargetHeap) {
    constexpr int mutatorsCount = 1;

    GCSchedulerConfig config;
    config.regularGcInterkonstMicroseconds = 10;
    config.autoTune = true;
    config.targetHeapBytes = 10;
    config.targetHeapUtilization = 0.5;
    config.minHeapBytes = 5;
    config.maxHeapBytes = 50;
    GCSchedulerDataOnSafepointsTestApi<mutatorsCount> schedulerTestApi(config);

    EXPECT_CALL(schedulerTestApi.scheduleGC(), Call());
    schedulerTestApi.Allocate(0, 10).get();
    testing::Mock::VerifyAndClearExpectations(&schedulerTestApi.scheduleGC());
    schedulerTestApi.OnPerformFullGC();
    schedulerTestApi.UpdateAliveSetBytes(10);

    EXPECT_THAT(config.targetHeapBytes.load(), 20);

    EXPECT_CALL(schedulerTestApi.scheduleGC(), Call());
    // For a total heap of 20.
    schedulerTestApi.Allocate(0, 10).get();
    testing::Mock::VerifyAndClearExpectations(&schedulerTestApi.scheduleGC());
    schedulerTestApi.OnPerformFullGC();
    schedulerTestApi.UpdateAliveSetBytes(20);

    EXPECT_THAT(config.targetHeapBytes.load(), 40);

    EXPECT_CALL(schedulerTestApi.scheduleGC(), Call());
    // For a total heap of 60.
    schedulerTestApi.Allocate(0, 40).get();
    testing::Mock::VerifyAndClearExpectations(&schedulerTestApi.scheduleGC());
    schedulerTestApi.OnPerformFullGC();
    schedulerTestApi.UpdateAliveSetBytes(60);

    // But we will keep the 50, which means we will trigger GC every allocation, until alive set falls down
    EXPECT_THAT(config.targetHeapBytes.load(), 50);

    EXPECT_CALL(schedulerTestApi.scheduleGC(), Call());
    // Keeping total heap of 60.
    schedulerTestApi.Allocate(0, 0).get();
    testing::Mock::VerifyAndClearExpectations(&schedulerTestApi.scheduleGC());
    schedulerTestApi.OnPerformFullGC();
    schedulerTestApi.UpdateAliveSetBytes(60);

    EXPECT_THAT(config.targetHeapBytes.load(), 50);

    EXPECT_CALL(schedulerTestApi.scheduleGC(), Call());
    schedulerTestApi.Allocate(0, 0).get();
    testing::Mock::VerifyAndClearExpectations(&schedulerTestApi.scheduleGC());
    schedulerTestApi.OnPerformFullGC();
    // Dropping to 40
    schedulerTestApi.UpdateAliveSetBytes(40);

    EXPECT_THAT(config.targetHeapBytes.load(), 50);

    EXPECT_CALL(schedulerTestApi.scheduleGC(), Call());
    // For a total heap of 50
    schedulerTestApi.Allocate(0, 10).get();
    testing::Mock::VerifyAndClearExpectations(&schedulerTestApi.scheduleGC());
    schedulerTestApi.OnPerformFullGC();
    // Dropping to 1
    schedulerTestApi.UpdateAliveSetBytes(1);

    // But the minimum is set to 5.
    EXPECT_THAT(config.targetHeapBytes.load(), 5);
}

TEST_F(GCSchedulerDataWithTimerTest, CollectOnTargetHeapReached) {
    constexpr int mutatorsCount = kDefaultThreadCount;

    GCSchedulerConfig config;
    config.regularGcInterkonstMicroseconds = 10;
    config.autoTune = false;
    config.targetHeapBytes = (mutatorsCount + 1) * 10;
    GCSchedulerDataWithTimerTestApi<mutatorsCount> schedulerTestApi(config);

    EXPECT_CALL(schedulerTestApi.scheduleGC(), Call()).Times(0);
    std_support::vector<std::future<void>> futures;
    for (int i = 0; i < mutatorsCount; ++i) {
        futures.push_back(schedulerTestApi.Allocate(i, 10));
    }
    for (auto& future : futures) {
        future.get();
    }
    testing::Mock::VerifyAndClearExpectations(&schedulerTestApi.scheduleGC());

    EXPECT_CALL(schedulerTestApi.scheduleGC(), Call());
    schedulerTestApi.Allocate(0, 10).get();
    testing::Mock::VerifyAndClearExpectations(&schedulerTestApi.scheduleGC());
    schedulerTestApi.OnPerformFullGC();
    schedulerTestApi.UpdateAliveSetBytes(0);

    EXPECT_CALL(schedulerTestApi.scheduleGC(), Call()).Times(0);
    schedulerTestApi.Allocate(0, 10).get();
    testing::Mock::VerifyAndClearExpectations(&schedulerTestApi.scheduleGC());

    EXPECT_CALL(schedulerTestApi.scheduleGC(), Call());
    schedulerTestApi.Allocate(0, mutatorsCount * 10).get();
    testing::Mock::VerifyAndClearExpectations(&schedulerTestApi.scheduleGC());
    testing::Mock::VerifyAndClearExpectations(&schedulerTestApi.scheduleGC());
    schedulerTestApi.OnPerformFullGC();
    schedulerTestApi.UpdateAliveSetBytes(0);
}

TEST_F(GCSchedulerDataWithTimerTest, CollectOnTimeoutReached) {
    constexpr int mutatorsCount = kDefaultThreadCount;

    GCSchedulerConfig config;
    config.regularGcInterkonstMicroseconds = 10;
    config.autoTune = false;
    config.targetHeapBytes = std::numeric_limits<size_t>::max();
    GCSchedulerDataWithTimerTestApi<mutatorsCount> schedulerTestApi(config);

    // Wait until the timer is initialized.
    test_support::manual_clock::waitForPending(test_support::manual_clock::now() + microseconds(10));

    EXPECT_CALL(schedulerTestApi.scheduleGC(), Call());
    schedulerTestApi.advance_time(microseconds(10));
    test_support::manual_clock::waitForPending(test_support::manual_clock::now() + microseconds(10));
    testing::Mock::VerifyAndClearExpectations(&schedulerTestApi.scheduleGC());
    schedulerTestApi.OnPerformFullGC();
    schedulerTestApi.UpdateAliveSetBytes(0);
}

TEST_F(GCSchedulerDataWithTimerTest, FullTimeoutAfterLastGC) {
    constexpr int mutatorsCount = kDefaultThreadCount;

    GCSchedulerConfig config;
    config.regularGcInterkonstMicroseconds = 10;
    config.autoTune = false;
    config.targetHeapBytes = 10;
    GCSchedulerDataWithTimerTestApi<mutatorsCount> schedulerTestApi(config);

    // Wait until the timer is initialized.
    test_support::manual_clock::waitForPending(test_support::manual_clock::now() + microseconds(10));

    schedulerTestApi.advance_time(microseconds(5));
    EXPECT_CALL(schedulerTestApi.scheduleGC(), Call());
    schedulerTestApi.Allocate(0, 10).get();
    testing::Mock::VerifyAndClearExpectations(&schedulerTestApi.scheduleGC());
    schedulerTestApi.OnPerformFullGC();
    schedulerTestApi.UpdateAliveSetBytes(0);

    // pending should restart to be 10us since the previous collection without scheduling another GC.
    EXPECT_CALL(schedulerTestApi.scheduleGC(), Call()).Times(0);
    test_support::manual_clock::waitForPending(test_support::manual_clock::now() + microseconds(10));
    testing::Mock::VerifyAndClearExpectations(&schedulerTestApi.scheduleGC());
}

TEST_F(GCSchedulerDataWithTimerTest, DoNotTuneTargetHeap) {
    constexpr int mutatorsCount = 1;

    GCSchedulerConfig config;
    config.regularGcInterkonstMicroseconds = 10;
    config.autoTune = false;
    config.targetHeapBytes = 10;
    GCSchedulerDataWithTimerTestApi<mutatorsCount> schedulerTestApi(config);

    EXPECT_CALL(schedulerTestApi.scheduleGC(), Call());
    schedulerTestApi.Allocate(0, 10).get();
    testing::Mock::VerifyAndClearExpectations(&schedulerTestApi.scheduleGC());
    schedulerTestApi.OnPerformFullGC();
    schedulerTestApi.UpdateAliveSetBytes(10);

    EXPECT_THAT(config.targetHeapBytes.load(), 10);
}

TEST_F(GCSchedulerDataWithTimerTest, TuneTargetHeap) {
    constexpr int mutatorsCount = 1;

    GCSchedulerConfig config;
    config.regularGcInterkonstMicroseconds = 10;
    config.autoTune = true;
    config.targetHeapBytes = 10;
    config.targetHeapUtilization = 0.5;
    config.minHeapBytes = 5;
    config.maxHeapBytes = 50;
    GCSchedulerDataWithTimerTestApi<mutatorsCount> schedulerTestApi(config);

    EXPECT_CALL(schedulerTestApi.scheduleGC(), Call());
    schedulerTestApi.Allocate(0, 10).get();
    testing::Mock::VerifyAndClearExpectations(&schedulerTestApi.scheduleGC());
    schedulerTestApi.OnPerformFullGC();
    schedulerTestApi.UpdateAliveSetBytes(10);

    EXPECT_THAT(config.targetHeapBytes.load(), 20);

    EXPECT_CALL(schedulerTestApi.scheduleGC(), Call());
    // For a total heap of 20.
    schedulerTestApi.Allocate(0, 10).get();
    testing::Mock::VerifyAndClearExpectations(&schedulerTestApi.scheduleGC());
    schedulerTestApi.OnPerformFullGC();
    schedulerTestApi.UpdateAliveSetBytes(20);

    EXPECT_THAT(config.targetHeapBytes.load(), 40);

    EXPECT_CALL(schedulerTestApi.scheduleGC(), Call());
    // For a total heap of 60.
    schedulerTestApi.Allocate(0, 40).get();
    testing::Mock::VerifyAndClearExpectations(&schedulerTestApi.scheduleGC());
    schedulerTestApi.OnPerformFullGC();
    schedulerTestApi.UpdateAliveSetBytes(60);

    // But we will keep the 50, which means we will trigger GC every allocation, until alive set falls down
    EXPECT_THAT(config.targetHeapBytes.load(), 50);

    EXPECT_CALL(schedulerTestApi.scheduleGC(), Call());
    // Keeping total heap of 60.
    schedulerTestApi.Allocate(0, 0).get();
    testing::Mock::VerifyAndClearExpectations(&schedulerTestApi.scheduleGC());
    schedulerTestApi.OnPerformFullGC();
    schedulerTestApi.UpdateAliveSetBytes(60);

    EXPECT_THAT(config.targetHeapBytes.load(), 50);

    EXPECT_CALL(schedulerTestApi.scheduleGC(), Call());
    schedulerTestApi.Allocate(0, 0).get();
    testing::Mock::VerifyAndClearExpectations(&schedulerTestApi.scheduleGC());
    schedulerTestApi.OnPerformFullGC();
    // Dropping to 40
    schedulerTestApi.UpdateAliveSetBytes(40);

    EXPECT_THAT(config.targetHeapBytes.load(), 50);

    EXPECT_CALL(schedulerTestApi.scheduleGC(), Call());
    // For a total heap of 50
    schedulerTestApi.Allocate(0, 10).get();
    testing::Mock::VerifyAndClearExpectations(&schedulerTestApi.scheduleGC());
    schedulerTestApi.OnPerformFullGC();
    // Dropping to 1
    schedulerTestApi.UpdateAliveSetBytes(1);

    // But the minimum is set to 5.
    EXPECT_THAT(config.targetHeapBytes.load(), 5);
}

TEST_F(GCSchedulerDataWithTimerTest, DoNotCollectOnTimerInBackground) {
    constexpr int mutatorsCount = kDefaultThreadCount;

    GCSchedulerConfig config;
    config.regularGcInterkonstMicroseconds = 10;
    config.autoTune = false;
    config.targetHeapBytes = std::numeric_limits<size_t>::max();
    GCSchedulerDataWithTimerTestApi<mutatorsCount> schedulerTestApi(config);

    // TODO: Not a global, please.
    mm::AppStateTrackingTestSupport appStateTracking(mm::GlobalData::Instance().appStateTracking());

    // Wait until the timer is initialized.
    test_support::manual_clock::waitForPending(test_support::manual_clock::now() + microseconds(10));

    // Now go into the background.
    ASSERT_THAT(mm::GlobalData::Instance().appStateTracking().state(), mm::AppStateTracking::State::kForeground);
    appStateTracking.setState(mm::AppStateTracking::State::kBackground);

    // Timer works in the background, but does nothing.
    EXPECT_CALL(schedulerTestApi.scheduleGC(), Call()).Times(0);
    schedulerTestApi.advance_time(microseconds(10));
    test_support::manual_clock::waitForPending(test_support::manual_clock::now() + microseconds(10));
    testing::Mock::VerifyAndClearExpectations(&schedulerTestApi.scheduleGC());

    // Now go back into the foreground.
    appStateTracking.setState(mm::AppStateTracking::State::kForeground);

    EXPECT_CALL(schedulerTestApi.scheduleGC(), Call());
    schedulerTestApi.advance_time(microseconds(10));
    test_support::manual_clock::waitForPending(test_support::manual_clock::now() + microseconds(10));
    testing::Mock::VerifyAndClearExpectations(&schedulerTestApi.scheduleGC());
    schedulerTestApi.OnPerformFullGC();
    schedulerTestApi.UpdateAliveSetBytes(0);
}

// These tests require a stack trace to contain call site addresses but
// on Windows a trace contains function addresses instead.
// So skip these tests on Windows.
#if (__MINGW32__ || __MINGW64__)
#define SKIP_ON_WINDOWS() do { GTEST_SKIP() << "Skip on Windows"; } while(false)
#else
#define SKIP_ON_WINDOWS() do { } while(false)
#endif

TEST(SafePointTrackerTest, RegisterSafePoints) {
    SKIP_ON_WINDOWS();
    []() OPTNONE {
        internal::SafePointTracker<> tracker;

        for (size_t i = 0; i < 10; i++) {
            bool registered1 = tracker.registerCurrentSafePoint(0);
            bool registered2 = tracker.registerCurrentSafePoint(0);

            bool expected = (i == 0);

            EXPECT_THAT(registered1, expected);
            EXPECT_THAT(registered2, expected);
        }
    }();
}

template <size_t SafePointStackSize>
OPTNONE bool registerCurrentSafePoint(internal::SafePointTracker<SafePointStackSize>& tracker) {
    return tracker.registerCurrentSafePoint(0);
}

TEST(SafePointTrackerTest, TrackTopFramesOnly) {
    SKIP_ON_WINDOWS();
    []() OPTNONE {
        internal::SafePointTracker<16> longTracker;
        internal::SafePointTracker<1> shortTracker;

        bool longRegistered1 = registerCurrentSafePoint(longTracker);
        bool longRegistered2 = registerCurrentSafePoint(longTracker);

        EXPECT_THAT(longRegistered1, true);
        EXPECT_THAT(longRegistered2, true);

        bool shortRegistered1 = registerCurrentSafePoint(shortTracker);
        bool shortRegistered2 = registerCurrentSafePoint(shortTracker);

        EXPECT_THAT(shortRegistered1, true);
        EXPECT_THAT(shortRegistered2, false);
    }();
}

TEST(SafePointTrackerTest, CleanOnSizeLimit) {
    SKIP_ON_WINDOWS();
    []() OPTNONE {
        internal::SafePointTracker<> tracker(2);

        ASSERT_THAT(tracker.size(), 0);
        ASSERT_THAT(tracker.maxSize(), 2);

        for (size_t i = 0; i < 3; i++) {
            bool registered1 = tracker.registerCurrentSafePoint(0);

            EXPECT_THAT(registered1, true);
            EXPECT_THAT(tracker.size(), 1);

            bool registered2 = tracker.registerCurrentSafePoint(0);

            EXPECT_THAT(registered2, true);
            EXPECT_THAT(tracker.size(), 2);
        }
    }();
}

TEST(AggressiveSchedulerTest, TriggerGCOnUniqueSafePoint) {
    SKIP_ON_WINDOWS();
    []() OPTNONE {
        testing::MockFunction<void()> scheduleGC;

        GCSchedulerConfig config;
        gc::internal::GCSchedulerDataAggressive scheduler(config, scheduleGC.AsStdFunction());
        ASSERT_EQ(config.threshold, 1);

        GCSchedulerThreadData threadSchedulerData(config, [](GCSchedulerThreadData&){});

        EXPECT_CALL(scheduleGC, Call()).Times(1);
        for (int i = 0; i < 10; i++) {
            scheduler.UpdateFromThreadData(threadSchedulerData);
        }
        testing::Mock::VerifyAndClearExpectations(&scheduleGC);

        EXPECT_CALL(scheduleGC, Call()).Times(1);
        scheduler.UpdateFromThreadData(threadSchedulerData);
        testing::Mock::VerifyAndClearExpectations(&scheduleGC);
    }();
}

TEST(AggressiveSchedulerTest, TriggerGCOnAllocationThreshold) {
    SKIP_ON_WINDOWS();
    []() OPTNONE {
        testing::MockFunction<void()> scheduleGC;

        GCSchedulerConfig config;
        gc::internal::GCSchedulerDataAggressive scheduler(config, scheduleGC.AsStdFunction());
        GCSchedulerThreadData threadSchedulerData(config, [&scheduler](GCSchedulerThreadData& data){
            scheduler.UpdateFromThreadData(data);
        });

        ASSERT_EQ(config.allocationThresholdBytes, 1);

        config.autoTune = false;
        config.targetHeapBytes = 10;

        int i = 0;
        // We trigger GC on the first iteration, when the unique allocation point is faced,
        // and on the last iteration when target heap size is reached.
        EXPECT_CALL(scheduleGC, Call())
            .WillOnce([&i]() { EXPECT_THAT(i, 0); })
            .WillOnce([&i]() { EXPECT_THAT(i, 9); });

        for (; i < 10; i++) {
            threadSchedulerData.OnSafePointAllocation(1);
        }
        testing::Mock::VerifyAndClearExpectations(&scheduleGC);
    }();
}

} // namespace gc
} // namespace kotlin
