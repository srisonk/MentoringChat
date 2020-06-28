package com.example.mentoringchat.ModelClasses

class Chat
{
    private var Id_sender: String = ""
    private var Content: String = ""
    private var Id_receiver: String = ""
    private var isseen = false
    private var url: String = ""
    private var messageId: String = ""

    constructor()


    constructor(
        Id_sender: String,
        Content: String,
        Id_receiver: String,
        isseen: Boolean,
        url: String,
        messageId: String
    ) {
        this.Id_sender = Id_sender
        this.Content = Content
        this.Id_receiver = Id_receiver
        this.isseen = isseen
        this.url = url
        this.messageId = messageId
    }

    fun getSender(): String?{
        return Id_sender
    }

    fun setSender(Id_sender: String?){
        this.Id_sender = Id_sender!!
    }

    fun getMessage(): String?{
        return Content
    }

    fun setMessage(Content: String?){
        this.Content = Content!!
    }

    fun getReceiver(): String?{
        return Id_receiver
    }

    fun setReceiver(Id_receiver: String?){
        this.Id_receiver = Id_receiver!!
    }

    fun isIsSeen(): Boolean{
        return isseen
    }

    fun setIsSeen(isseen: Boolean?){
        this.isseen = isseen!!
    }

    fun getUrl(): String?{
        return url
    }

    fun setUrl(url: String?){
        this.url = url!!
    }

    fun getMessageId(): String?{
        return messageId
    }

    fun setMessageId(messageId: String?){
        this.messageId = messageId!!
    }


}