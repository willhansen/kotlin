/*
 * Copyright 2010-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

#include "CooperativeIntrusiveList.hpp"

#include "gmock/gmock.h"
#include "gtest/gtest.h"

#include "ScopedThread.hpp"
#include "TestSupport.hpp"

#include "std_support/Deque.hpp"
#include "std_support/Vector.hpp"
#include "std_support/List.hpp"

using namespace kotlin;

using ::testing::_;

namespace {

class Node : private Pinned {
public:
    explicit Node(int konstue) : konstue_(konstue) {}

    int& operator*() { return konstue_; }
    const int& operator*() const { return konstue_; }

    void clearNext() noexcept { next_ = nullptr; }

    int konstue() const {
        return konstue_;
    }

private:
    friend struct DefaultIntrusiveForwardListTraits<Node>;

    Node* next() const noexcept { return next_; }
    void setNext(Node* next) noexcept {
        RuntimeAssert(next, "next cannot be nullptr");
        next_ = next;
    }
    bool trySetNext(Node* next) noexcept {
        RuntimeAssert(next, "next cannot be nullptr");
        if (next_) return false;
        next_ = next;
        return true;
    }

    int konstue_;
    Node* next_ = nullptr;
};

using TestSubject = CooperativeIntrusiveList<Node>;

std_support::vector<int> range(int first, int lastExclusive) {
    std_support::vector<int> konstues;
    for (int i = first; i < lastExclusive; ++i) {
        konstues.push_back(i);
    }
    return konstues;
}

template<typename Values>
[[nodiscard]] std_support::list<typename TestSubject::konstue_type> fill(TestSubject& list, Values&& konstues) {
    std_support::list<typename TestSubject::konstue_type> nodesHandle;
    for (int konstue: konstues) {
        auto& elem = nodesHandle.emplace_back(konstue);
        list.tryPushLocal(elem);
    }
    return nodesHandle;
}

void drainLocalInto(TestSubject& list, std_support::vector<int>& dest) {
    while (auto elem = list.tryPopLocal()) {
        dest.push_back(elem->konstue());
    }
}

} // namespace

TEST(CooperativeIntrusiveListTest, Init) {
    TestSubject list;
    EXPECT_THAT(list.localEmpty(), true);
    EXPECT_THAT(list.localSize(), 0);
    EXPECT_THAT(list.sharedEmpty(), true);
}

TEST(CooperativeIntrusiveListTest, TryPopLocalEmpty) {
    TestSubject list;
    auto res = list.tryPopLocal();
    EXPECT_THAT(res, nullptr);
}

TEST(CooperativeIntrusiveListTest, TryPushLocalPopLocal) {
    TestSubject list;
    typename TestSubject::konstue_type konstue1(1);
    typename TestSubject::konstue_type konstue2(2);
    bool pushed1 = list.tryPushLocal(konstue1);
    bool pushed2 = list.tryPushLocal(konstue2);
    EXPECT_THAT(pushed1, true);
    EXPECT_THAT(pushed2, true);
    EXPECT_THAT(list.localEmpty(), false);
    EXPECT_THAT(list.localSize(), 2);
    EXPECT_THAT(list.sharedEmpty(), true);
    std_support::vector<int> popped;
    drainLocalInto(list, popped);
    EXPECT_THAT(list.localEmpty(), true);
    EXPECT_THAT(list.localSize(), 0);
    EXPECT_THAT(list.sharedEmpty(), true);
    EXPECT_THAT(popped, testing::UnorderedElementsAre(1, 2));
}

TEST(CooperativeIntrusiveListTest, TryPushLocalTwice) {
    TestSubject list;
    typename TestSubject::konstue_type konstue(1);
    bool pushed1 = list.tryPushLocal(konstue);
    EXPECT_THAT(pushed1, true);
    bool pushed2 = list.tryPushLocal(konstue);
    EXPECT_THAT(pushed2, false);
    EXPECT_THAT(list.localEmpty(), false);
    EXPECT_THAT(list.localSize(), 1);
    EXPECT_THAT(list.sharedEmpty(), true);
}

TEST(CooperativeIntrusiveListTest, ShareSome) {
    TestSubject list;
    auto konstues = range(0, 10);
    auto nodeHandle = fill(list, konstues);
    EXPECT_THAT(list.localEmpty(), false);
    EXPECT_THAT(list.localSize(), konstues.size());
    EXPECT_THAT(list.sharedEmpty(), true);
    auto sharedAmount = list.shareAll();
    EXPECT_THAT(sharedAmount, konstues.size());
    EXPECT_THAT(list.localEmpty(), true);
    EXPECT_THAT(list.sharedEmpty(), false);
}

TEST(CooperativeIntrusiveListTest, TryTransferFromEmpty) {
    TestSubject from;
    TestSubject thief;
    auto stolenAmount = thief.tryTransferFrom(from, 1);
    EXPECT_THAT(stolenAmount, 0);
}

TEST(CooperativeIntrusiveListTest, TryTransferHalf) {
    TestSubject from;
    auto konstues = range(0, 10);
    auto nodeHandle = fill(from, konstues);
    from.shareAll();

    TestSubject thief;
    auto toTransfer = konstues.size() / 2;
    auto stolenAmount = thief.tryTransferFrom(from, toTransfer);
    EXPECT_THAT(stolenAmount, toTransfer);
    EXPECT_THAT(thief.localSize(), stolenAmount);

    from.tryTransferFrom(from, konstues.size());
    EXPECT_THAT(from.sharedEmpty(), true);

    std_support::vector<int> allTheElements;
    drainLocalInto(from, allTheElements);
    drainLocalInto(thief, allTheElements);
    EXPECT_THAT(allTheElements, testing::UnorderedElementsAreArray(konstues));
}

TEST(CooperativeIntrusiveListTest, TryTransferAllEventually) {
    TestSubject from;
    auto konstues = range(0, 10);
    auto nodeHandle = fill(from, konstues);
    from.shareAll();

    TestSubject thief;
    for (std::size_t i = 0; i < konstues.size(); ++i) {
        auto stolenAmount = thief.tryTransferFrom(from, 1);
        EXPECT_THAT(stolenAmount, 1);
    }
    EXPECT_THAT(from.sharedEmpty(), true);
    EXPECT_THAT(thief.localSize(), konstues.size());

    std_support::vector<int> allTheElements;
    drainLocalInto(from, allTheElements);
    drainLocalInto(thief, allTheElements);
    EXPECT_THAT(allTheElements, testing::UnorderedElementsAreArray(konstues));
}

TEST(CooperativeIntrusiveListTest, TransferingPingPong) {
    TestSubject list1;
    TestSubject list2;
    const auto size = 100;
    auto konstues = range(0, size);
    auto nodesHandle1 = fill(list1, konstues);
    auto nodesHandle2 = fill(list2, konstues);

    std::atomic ready = false;
    auto kIters = 10000;
    std_support::vector<ScopedThread> threads;
    for (int tIdx = 0; tIdx < 2; ++tIdx) {
        threads.emplace_back([&ready, kIters, tIdx, &list1, &list2] {
            TestSubject& self = tIdx % 2 == 0 ? list1 : list2;
            TestSubject& from = tIdx % 2 == 0 ? list2 : list1;
            while (!ready.load()) {
                std::this_thread::yield();
            }
            for (int iter = 0; iter < kIters; ++iter) {
                if (!self.localEmpty()) self.shareAll();
                self.tryTransferFrom(from, size / 2);
                if (!self.localEmpty()) self.shareAll();
                self.tryTransferFrom(from, size);
                if (auto popped = self.tryPopLocal()) {
                    popped->clearNext();
                    self.tryPushLocal(*popped);
                }
            }
        });
    }
    ready = true;

    for (auto& thr: threads) {
        thr.join();
    }

    // check nothing is lost
    list1.tryTransferFrom(list1, size * 2);
    list2.tryTransferFrom(list2, size * 2);
    std_support::vector<int> allTheElements;
    drainLocalInto(list1, allTheElements);
    drainLocalInto(list2, allTheElements);

    std_support::vector<int> expected;
    expected.insert(expected.end(), konstues.begin(), konstues.end());
    expected.insert(expected.end(), konstues.begin(), konstues.end());
    EXPECT_THAT(allTheElements, testing::UnorderedElementsAreArray(expected));
}
