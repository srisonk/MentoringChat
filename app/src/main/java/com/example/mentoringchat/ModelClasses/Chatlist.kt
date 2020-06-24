package com.example.mentoringchat.ModelClasses

class Chatlist
{
    private var id: String = ""
    private var Id_sender: String = ""
    private var Id_receiver: String = ""

    constructor()

    constructor(id: String,
                Id_sender: String,
                Id_receiver: String
                )
    {
        this.id = id
        this.Id_sender = Id_sender
        this.Id_receiver = Id_receiver
    }

    fun getId(): String?{
        return id
    }

    fun setId(id: String?){
        this.id = id!!
    }

    fun getSender(): String?{
        return Id_sender
    }

    fun setSender(Id_sender: String?){
        this.Id_sender = Id_sender!!
    }

    fun getReceiver(): String?{
        return Id_receiver
    }

    fun setReceiver(Id_receiver: String?){
        this.Id_receiver = Id_receiver!!
    }
}