package `in`.nakkalites.mediaclient.data.user

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(val id: String, val name: String, val email: String)

@JsonClass(generateAdapter = true)
data class UserResponse(@field:Json(name = "user") val user: UserEntity)

@JsonClass(generateAdapter = true)
data class UserEntity(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "name") val name: String,
    @field:Json(name = "email") val email: String
)
