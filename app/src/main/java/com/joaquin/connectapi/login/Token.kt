package com.joaquin.connectapi.login

data class Token(
    val accessToken: AccessToken,
    val plainTextToken: String
)