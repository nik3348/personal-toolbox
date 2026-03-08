package com.atebitstack.voidbox.reminders

import kotlinx.serialization.Serializable

@Serializable
data class RocketLaunch(
    val flightNumber: Int,
    val missionName: String,
    val launchYear: Int,
    val launchDateUTC: String,
    val rocket: Rocket,
    val launchSuccess: Boolean?,
    val details: String?,
)

@Serializable
data class Rocket(
    val id: String,
    val name: String,
    val type: String,
)
