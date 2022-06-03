package com.example.mapsapp.fragments

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mapsapp.R
import com.example.mapsapp.controllers.Database
import com.example.mapsapp.controllers.UsersAdapter
import com.example.mapsapp.data.User
import com.example.mapsapp.data.UserData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


class Users : Fragment() {
    private val userList = ArrayList<User>()
    private val database = Database()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_users, container, false)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = UsersAdapter(view.context, userList)
        val recyclerView = view.findViewById<RecyclerView>(R.id.list)
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        recyclerView.adapter = adapter
        database.parties.child(UserData.party).child("listOfPeople").addValueEventListener( object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (user in snapshot.children){
                    val userGuest = User()
                    userList.add(userGuest)
                    database.users.child(user.value.toString()).get().addOnSuccessListener {
                        userGuest.name = it.child("name").value.toString()
                        userGuest.description = it.child("description").value.toString()
                        adapter.notifyDataSetChanged()
                    }
                    database.usersImage.child(user.value.toString()).downloadUrl.addOnSuccessListener {
                        userGuest.photoUri = it
                        adapter.notifyDataSetChanged()
                    }

                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        super.onViewCreated(view, savedInstanceState)
    }
}