package com.whale.api.user.adapter.`in`.web.response

data class UpdateTokenResponse(
    val message: String,
) {
    companion object {
        fun success(): UpdateTokenResponse {
            return UpdateTokenResponse(
                message = "Token updated successfully",
            )
        }
    }
}
