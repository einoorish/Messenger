package com.example.messenger.messages

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.messenger.R
import com.example.messenger.messages.MessagesActivity.Companion.currentUser
import com.example.messenger.models.ChatMessage
import com.example.messenger.models.User
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.chat_row_left.view.*
import kotlinx.android.synthetic.main.chat_row_right.view.*

class ChatActivity : AppCompatActivity() {

    val adapter = GroupAdapter<ViewHolder>()

    var toUser = User()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        chat_log_rv.adapter = adapter

         toUser = User(
             intent.getStringExtra(NewMessageActivity.UID_KEY)!!,
             intent.getStringExtra(NewMessageActivity.USERNAME_KEY)!!,
             intent.getStringExtra(NewMessageActivity.PHOTO_KEY)!!
        )

        supportActionBar?.title = toUser.username


       // setupChatLog(adapter)
        listenForMessages()


        btn_send.setOnClickListener {
            if(!enter_message_field.text.isEmpty())
                performSendingMessage(toUser)
        }

    }

    private fun performSendingMessage(user:User){

        val text = enter_message_field.text.toString()

        val fromId = currentUser!!.uid
        val toId = user.uid

        //val reference = FirebaseDatabase.getInstance().getReference("/messages").push()
        val referenceFrom = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val referenceTo = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        var chatMessage = ChatMessage(referenceFrom.key.toString(),text,fromId, toId, System.currentTimeMillis()/1000)

        referenceFrom.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d("ChatActivity", "Message saved to Firebase")
                enter_message_field.text = null
                chat_log_rv.scrollToPosition(adapter.itemCount - 1)
            }

        if(toId!=fromId)
            referenceTo.setValue(chatMessage)

        val latestMessagesRefFrom = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        val latestMessagesRefTo = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latestMessagesRefFrom.setValue(chatMessage)
        if(toId!=fromId)
            latestMessagesRefTo.setValue(chatMessage)

    }

    private fun listenForMessages(){

        val fromId = currentUser!!.uid
        val toId = toUser.uid

        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
        ref.addChildEventListener(object: ChildEventListener{

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)

                if(chatMessage!=null){

                    var currentUser = currentUser

                    if(chatMessage.fromId == currentUser!!.uid ){
                        adapter.add(ChatItemRight(chatMessage.text,currentUser))
                    } else if(chatMessage.fromId == toUser.uid){
                        adapter.add(ChatItemLeft(chatMessage.text, toUser))
                    }
                }
                chat_log_rv.scrollToPosition(adapter.itemCount - 1)
            }
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

        })
    }

}

class ChatItemLeft(private val text: String, private val user: User): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.log_message_left.text = text

        Picasso.get().load(user.profilePhotoUrl).resize(80,80).centerCrop().into(viewHolder.itemView.left_log_message_photo)
    }

    override fun getLayout(): Int {
        return R.layout.chat_row_left
    }

}

class ChatItemRight(private val text: String, private val user:User): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.log_message_right.text = text

        Picasso.get().load(user.profilePhotoUrl).resize(80,80).centerCrop().into(viewHolder.itemView.log_message_photo)
    }

    override fun getLayout(): Int {
        return R.layout.chat_row_right
    }
}



