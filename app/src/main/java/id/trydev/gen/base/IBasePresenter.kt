package id.trydev.gen.base

interface IBasePresenter<in V: IBaseView> {
    fun attachView(mRootView: V)
    fun detachView()
}