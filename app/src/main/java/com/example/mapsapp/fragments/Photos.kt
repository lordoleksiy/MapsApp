package com.example.mapsapp.fragments

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.service.autofill.UserData
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mapsapp.R
import com.example.mapsapp.controllers.Database
import com.example.mapsapp.controllers.PhotoAdapter

class Photos : Fragment() {
    private val imageList = ArrayList<Uri>()
    private val nameList = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_photos, container, false)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = PhotoAdapter(view.context, imageList, nameList)
        val recyclerView = view.findViewById<RecyclerView>(R.id.photoGallery)
        val gridLayoutManager = GridLayoutManager(view.context, 3, GridLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = gridLayoutManager
        recyclerView.adapter = adapter
        val database = Database()
        database.partiesImage.child(com.example.mapsapp.data.UserData.party).child("icons").listAll().addOnCompleteListener {items->
            for (uri in items.result.items){
                uri.downloadUrl.addOnCompleteListener {
                    nameList.add(uri.path)
                    imageList.add(it.result)
                    adapter.notifyDataSetChanged()
                }
            }
        }
        super.onViewCreated(view, savedInstanceState)
    }
}

