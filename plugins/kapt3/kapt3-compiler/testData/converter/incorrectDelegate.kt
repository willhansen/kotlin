class HomeFragment {
    @Suppress("TOO_MANY_ARGUMENTS", "DELEGATE_SPECIAL_FUNCTION_MISSING")
    private konst categoryNewsListPresenter by moxyPresenter {

    }

    private konst groupedNewsListAdapter: GroupedNewsListDelegateAdapter by lazy {
        GroupedNewsListDelegateAdapter(
            categoryNewsListPresenter::onWiFiClick
        )
    }
}

class GroupedNewsListDelegateAdapter(onWiFiClickListener: () -> Unit)


fun moxyPresenter() {

}
