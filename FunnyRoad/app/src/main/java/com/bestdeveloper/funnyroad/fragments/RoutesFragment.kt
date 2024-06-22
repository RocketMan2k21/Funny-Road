package com.bestdeveloper.funnyroad.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Visibility
import com.bestdeveloper.funnyroad.R
import com.bestdeveloper.funnyroad.activity.MapActivity
import com.bestdeveloper.funnyroad.databinding.RecycleViewRoutesBinding
import com.bestdeveloper.funnyroad.model.Route
import java.util.concurrent.Executors


class RoutesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var mapViewModel: MapViewModel
    private var routes: ArrayList<Route> = ArrayList()
    private lateinit var adapter: RoutesAdapter

    // Text view for displaying "No saved routes"
    private lateinit var noSavedRoutesTxt : TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_routes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            mapViewModel = ViewModelProvider(it).get(MapViewModel::class.java)
        }

        noSavedRoutesTxt = requireView().findViewById(R.id.route_frag_noSavedRoutes_textView)

        if(routes.isEmpty())
            displayAllRoutes()

        adapter = RoutesAdapter(routes)
        recyclerView = view.findViewById(R.id.recycleView_routeFrag)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        mapViewModel.routes.observe(viewLifecycleOwner, Observer {
            routes = it
            adapter.notifyDataSetChanged()
        })

        //Handling swipe
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val route = routes[viewHolder.adapterPosition]
                deleteRoute(route, viewHolder.adapterPosition)
            }
        }).attachToRecyclerView(recyclerView)
}
    private fun deleteRoute(route: Route, pos: Int) {
        routes.removeAt(pos)
        mapViewModel.deleteRoute(route)
        adapter.notifyDataSetChanged()
    }

    private fun displayAllRoutes(){
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())

        executor.execute(Runnable {
            routes.addAll(mapViewModel.routes.value as ArrayList)
        })

        handler.post(Runnable {
            if (routes.isEmpty()){
                noSavedRoutesTxt.visibility = TextView.VISIBLE
            }
            adapter.notifyDataSetChanged()
        })

    }


    inner class RoutesAdapter(
        private var routes: ArrayList<Route>
    ): RecyclerView.Adapter<RoutesFragment.MyViewHolder>() {

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
            val route = routes.get(position)
            holder.binding.route = route
            holder.itemView.setOnClickListener(View.OnClickListener {
                mapViewModel.route.value = route
                val mapActivity = activity as MapActivity
                mapActivity.replaceFragments(MapFragment::class.java)
                mapActivity.navigationViewToHome()

            })
        }
    }

    inner class MyViewHolder(
        val binding: RecycleViewRoutesBinding
    ): RecyclerView.ViewHolder(binding.root)
}