package id.trydev.gen.presenter

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import id.trydev.gen.contract.HomeContract
import id.trydev.gen.model.Lokasi
import id.trydev.gen.model.Wilayah


class HomePresenter: HomeContract.Presenter {
    private lateinit var view:HomeContract.View
    private lateinit var db:FirebaseFirestore
    private lateinit var sort : Array<Int>

    override fun getMarker() {
        view.showLoading()

        db.collection("lokasi")
            .get()
            .addOnCompleteListener{ task ->
                if(task.isSuccessful){
                    val arr: ArrayList<Map<String, Any>> = ArrayList()
                    var i = 0
                    for (document in task.result!!){
//                        arr.set(i, document.data)
//                        var w: Wilayah = document.toObject(Wilayah::class.java)
                        arr.add(document.data)
//                        Log.e("testing", document.id)
                    }
                    view.loadData(loadSort(arr))
                } else {
                    Log.e("error in get marker", "make error in home presenter",task.exception)
                }
            }
        view.dismissLoading()
    }

    override fun attachView(mRootView: HomeContract.View) {
        view = mRootView
        db = FirebaseFirestore.getInstance()
        sort = arrayOf(1, 2, 7, 21, 20, 18, 17, 19, 15, 14, 13, 8, 9, 10, 11, 12, 16, 5, 6, 4, 3)
    }

    override fun detachView() {
    }

    fun loadSort(arr : ArrayList<Map<String, Any>>) : ArrayList<Map<String, Any>> {
        var sort = getSort()
        var new = arrayListOf<Map<String, Any>>()
        for ((i, j) in sort.withIndex()){
            for ((index, value) in arr.withIndex()){
                if (j.toString() == value.get("wilayah").toString()){
                    new.add(value)
                }
            }
        }
        return new
    }

    fun getSort() : Array<Int>{
        return sort
    }
}