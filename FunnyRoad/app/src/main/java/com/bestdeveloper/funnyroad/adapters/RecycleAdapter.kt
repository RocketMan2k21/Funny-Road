package com.bestdeveloper.funnyroad.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bestdeveloper.funnyroad.R
import com.bestdeveloper.funnyroad.databinding.RecycleViewRoutesBinding
import com.bestdeveloper.funnyroad.model.Route
import com.bestdeveloper.funnyroad.service.ItemClickListener

class RecycleAdapter(
    private var routes: Array<Route>
): RecyclerView.Adapter<RecycleAdapter.MyViewHolder>() {
    lateinit var clickListener: ItemClickListener


    fun setOnClickListener(clickListener: ItemClickListener){
        this.clickListener = clickListener
    }


    inner class MyViewHolder(
        val binding: RecycleViewRoutesBinding): RecyclerView.ViewHolder(binding.root){
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding: RecycleViewRoutesBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.recycle_view_routes,
            parent,
            false
        )

        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return routes.size;
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val route: Route = routes.get(position)
        holder.binding.route = route
    }


}