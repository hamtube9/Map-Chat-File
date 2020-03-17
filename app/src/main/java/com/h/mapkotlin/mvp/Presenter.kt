package ds.vuongquocthanh.socialnetwork.mvp

interface Presenter {
    fun attachView(view: View)
    fun dispose()
}