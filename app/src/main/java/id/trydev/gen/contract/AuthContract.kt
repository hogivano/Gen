package id.trydev.gen.contract

import id.trydev.gen.base.IBaseView

interface AuthContract{
    interface View: IBaseView {
        fun onLogin(usrmail: String, password: String)
        fun onRegister(usrmail: String, password: String, confirmed_password: String)
        fun showError(msg: String, errorCode: Int)
    }
}