package `in`.nakkalites.mediaclient.data.user

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginResponse(
    @field:Json(name = "user") val user: UserEntity
)

@JsonClass(generateAdapter = true)
data class UserEntity(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "name") val name: String?,
    @field:Json(name = "email") val email: String?,
    @field:Json(name = "image_url") val imageUrl: String?,
    @field:Json(name = "access_token") val accessToken: String,
    @field:Json(name = "is_first_login") val isFirstLogin: Boolean,
    @field:Json(name = "signup_date") val signupDate: String
)
