@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.eightbitstack.toolbox

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform