package id.trydev.gen.base

open class BasePresenter <T: IBaseView> : IBasePresenter<T> {
    var mRootView: T? = null
        private set

    override fun detachView() {
        mRootView = null
    }

    override fun attachView(mRootView: T) {
        this.mRootView = mRootView
    }

    private val isViewAttached: Boolean
        get() = mRootView !== null

    fun checkViewAttached(){
        if (!isViewAttached) throw MvpViewNotAttachedException()
    }

    private class MvpViewNotAttachedException internal constructor() : RuntimeException("Please call IPresenter.attachView(IBaseView) before" + " requesting data to the IPresenter")

}
