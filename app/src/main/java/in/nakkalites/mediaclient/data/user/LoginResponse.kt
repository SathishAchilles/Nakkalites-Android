package `in`.nakkalites.mediaclient.data.user

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginResponse(
    @field:Json(name = "user") val user: LoginUserEntity
)

@JsonClass(generateAdapter = true)
data class LoginUserEntity(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "name") val name: String?,
    @field:Json(name = "email") val email: String?,
    @field:Json(name = "image_url") val imageUrl: String?,
    @field:Json(name = "refresh_token") val refreshToken: String,
    @field:Json(name = "access_token") val accessToken: String,
    @field:Json(name = "provider_type") val providerType: String?,
    @field:Json(name = "country_code") val countryCode: String?,
    @field:Json(name = "mobile") val phoneNumber: String?,
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
//{
//    "user": {
//        "id": 2,
//        "name": "Developer",
//        "email": "developer@developer.com",
//        "country_code": "+91",
//        "mobile": "0123456789",
//        "gender": null,
//        "country": null,
//        "city": null,
//        "dob": null,
//        "plan_uid": "prime"
//    },
//    "plan": {
//        "uid": "prime",
//        "name": "Prime",
//        "price": "49.0",
//        "frequency": "month",
//        "content_tags": [
//            "Premium"
//        ]
//    }
//}


@JsonClass(generateAdapter = true)
data class UserResponse(
    @field:Json(name = "user") val user: UserEntity,
    @field:Json(name = "plan") val plan: PlanEntity?
)

@JsonClass(generateAdapter = true)
data class UserEntity(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "name") val name: String?,
    @field:Json(name = "email") val email: String?,
    @field:Json(name = "image_url") val imageUrl: String?,
    @field:Json(name = "provider_type") val providerType: String?,
    @field:Json(name = "country_code") val countryCode: String?,
    @field:Json(name = "mobile") val phoneNumber: String?,
    @field:Json(name = "gender") val gender: String?,
    @field:Json(name = "city") val city: String?,
    @field:Json(name = "dob") val dob: String?,
    @field:Json(name = "country") val country: String?,
    @field:Json(name = "plan_uid") val planUid: String?,
)

@JsonClass(generateAdapter = true)
data class PlanEntity(
    @field:Json(name = "uid") val id: String?,
    @field:Json(name = "name") val name: String?,
    @field:Json(name = "price") val price: String?,
    @field:Json(name = "frequency") val frequency: String?,
    @field:Json(name = "content_tags") val contentTags: List<String>?,
)
