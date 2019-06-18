package id.trydev.gen.contract

import id.trydev.gen.base.IBasePresenter
import id.trydev.gen.base.IBaseView


interface HomeContract{
    interface View: IBaseView {
        fun loadData(arr:ArrayList<Map<String, Any>>)
        fun showError(msg: String, errorCode: Int)
    }

    interface Presenter: IBasePresenter<View> {
        fun getMarker()
    }
}