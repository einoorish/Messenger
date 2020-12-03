package com.example.messenger.messages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messenger.R
import com.example.messenger.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.item_new_message.view.*

class NewMessageActivity : AppCompatActivity() {

    companion object{
        const val USERNAME_KEY = "USER_KEY"
        const val PHOTO_KEY = "PHOTO_KEY"
        const val UID_KEY = "UID_KEY"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        supportActionBar?.title = "Select User"

        fetchUsers()

        recycler_view_newMessage.layoutManager = LinearLayoutManager(this)

    }

    private fun fetchUsers(){
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }
            override fun onDataChange(p0: DataSnapshot) {

                val adapter = GroupAdapter<ViewHolder>()

                //p0 is snapshot
                p0.children.forEach {
                    val user = it.getValue(User::class.java)
                    adapter.add(UserItem(user!!))
                }

                adapter.setOnItemClickListener{ item, view ->
                    val userItem = item as UserItem

                    val intent = Intent(view.context, ChatActivity::class.java)
                    //tried to do it with parcibles but failed :<
                    intent.putExtra(USERNAME_KEY, userItem.user.username)
                    intent.putExtra(PHOTO_KEY, userItem.user.profilePhotoUrl)
                    intent.putExtra(UID_KEY, userItem.user.uid)
                    startActivity(intent)

                    finish()
                }

                recycler_view_newMessage.adapter = adapter
            }
        })
    }


class UserItem(val user: User): Item<ViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.item_new_message
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.new_message_name.text = user.username
        Picasso.get().load(user.profilePhotoUrl).resize(80,80).centerCrop().centerCrop().into(viewHolder.itemView.new_message_photo)
    }

}

}
//TODO: add animaiton for rv items to appear smoothly(??)