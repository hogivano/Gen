package id.trydev.gen.model

data class User (
    private var key:String? = null,
    var nama: String? = null,
    var email: String? = null,
    var password: String? = null,
    var rePassword: String? = null
)