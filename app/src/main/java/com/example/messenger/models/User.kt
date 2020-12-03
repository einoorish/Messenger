package com.example.messenger.models

class User(val uid: String,val username: String, val profilePhotoUrl: String) {

    constructor():this("", "","")

}