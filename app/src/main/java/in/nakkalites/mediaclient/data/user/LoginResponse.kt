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
    @field:Json(name = "refresh_token") val refreshToken: String,
    @field:Json(name = "access_token") val accessToken: String,
    @field:Json(name = "provider_type") val providerType: String,
    @field:Json(name = "country_code") val countryCode: String?,
    @field:Json(name = "phone_number") val phoneNumber: String?,
    @field:Json(name = "gender") val gender: String?,
    @field:Json(name = "city") val city: String?,
    @field:Json(name = "dob") val dob: String?,
    @field:Json(name = "country") val country: String?,
    @field:Json(name = "is_first_login") val isFirstLogin: Boolean
)

@JsonClass(generateAdapter = true)
data class RefreshTokenResponse(
    @field:Json(name = "refresh_token") val refreshToken: String,
    @field:Json(name = "access_token") val accessToken: String
)
