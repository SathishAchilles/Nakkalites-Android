package `in`.nakkalites.mediaclient.data.user

import com.squareup.moshi.Json

data class User(val id: String, val name: String, val email: String)

data class UserEntity(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "name") val name: String
)
