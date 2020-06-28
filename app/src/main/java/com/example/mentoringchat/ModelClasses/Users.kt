package com.example.mentoringchat.ModelClasses

class Users
{
    private var user_id: String = ""
    private var username: String = ""
    private var gender: String = ""
    private var nationality: String = ""
    private var password: String = ""
    private var birthdate: String = ""
    private var course_id: String = ""
    private var profile: String = ""
    private var cover: String = ""

    constructor()


    constructor(
        user_id: String,
        username: String,
        gender: String,
        nationality: String,
        password: String,
        birthdate: String,
        course_id: String,
        profile: String,
        cover: String
    ) {
        this.user_id = user_id
        this.username = username
        this.gender = gender
        this.nationality = nationality
        this.password = password
        this.birthdate = birthdate
        this.course_id = course_id
        this.profile = profile
        this.cover = cover
    }

    fun getUID(): String?{
        return user_id
    }

    fun setUID(user_id: String){
        this.user_id=user_id
    }

    fun getUsername(): String?{
        return username
    }

    fun setUsername(username: String){
        this.username=username
    }

    fun getGender(): String?{
        return gender
    }

    fun setGender(gender: String){
        this.gender=gender
    }

    fun getNationality(): String?{
        return nationality
    }

    fun setNationality(nationality: String){
        this.nationality=nationality
    }

    fun getPassword(): String?{
        return password
    }

    fun setPassword(password: String){
        this.password=password
    }

    fun getBirthdate(): String?{
        return birthdate
    }

    fun setBirthdate(birthdate: String){
        this.birthdate=birthdate
    }

    fun getCourseId(): String?{
        return course_id
    }

    fun setCourseId(course_id: String){
        this.course_id=course_id
    }

    fun getProfile(): String?{
        return profile
    }

    fun setProfile(profile: String){
        this.profile=profile
    }

    fun getCover(): String?{
        return cover
    }

    fun setCover(cover: String){
        this.cover=cover
    }
}