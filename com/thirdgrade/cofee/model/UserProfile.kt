package com.thirdgrade.cofee.model


import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "profiles")
data class UserProfile(
    @PrimaryKey
    val userId: String = UUID.randomUUID().toString(),
    val name: String,
    val bio: String,
    val interests: List<String>,
    val profilePicUri: String? = null,
    val isCurrentUser: Boolean = false,
    val discoveryTimestamp: Long = System.currentTimeMillis()
)