package id.trydev.gen.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.trydev.gen.R
import kotlinx.android.synthetic.main.list_detail.view.*

class DetailAdapter(private var list: ArrayList<Map<String, Any>>, private val context: Context) : RecyclerView.Adapter<Holder>(){
    override fun getItemCount(): Int = list.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.list_detail, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bindHolder(list.get(position), context)
    }

    fun changeList(a: ArrayList<Map<String, Any>>){
        this.list = a
        notifyDataSetChanged()
    }
}

class Holder(view: View) : RecyclerView.ViewHolder(view){
    val nama = view.nama
    val alamat = view.alamat
    fun bindHolder(map: Map<String, Any>, context: Context){
        nama.text = map.get("nama_pelanggan").toString()
        alamat.text = map.get("alamat").toString()
    }
}