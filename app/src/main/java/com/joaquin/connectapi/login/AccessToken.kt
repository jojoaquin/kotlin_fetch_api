package com.joaquin.connectapi.login

data class AccessToken(
    val abilities: List<String>,
    val created_at: String,
    val expires_at: String,
    val id: Int,
    val name: String,
    val tokenable_id: Int,
    val tokenable_type: String,
    val updated_at: String
)