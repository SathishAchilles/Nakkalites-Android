package `in`.nakkalites.mediaclient.data.user

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginResponse(
    @field:Json(name = "user") val user: UserEntity,
    @field:Json(name = "access_token") val accessToken: String?
)

@JsonClass(generateAdapter = true)
data class UserEntity(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "name") val name: String?,
    @field:Json(name = "email") val email: String?,
    @field:Json(name = "image_url") val imageUrl: String?
)
