package com.naman14.timberx

import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.font
import androidx.compose.ui.text.font.fontFamily
import androidx.compose.ui.unit.sp

val DarkColors = darkColors(
        primary = Color(66, 122, 221)
)

val LightColors = lightColors(
        primary = Color(66, 122, 221)
)

private val RubikRegular = fontFamily(
        font(R.font.rubik_regular),
)
private val RubikMedium = fontFamily(
        font(R.font.rubik_medium),
)

val TimberTypography = Typography(
        h6 = TextStyle(
                fontFamily = RubikMedium,
                fontSize = 20.sp
        ),
        body1 = TextStyle(
                fontFamily = RubikRegular,
                fontSize = 16.sp
        ),
        body2 = TextStyle(
                fontFamily = RubikRegular,
                fontSize = 14.sp
        ),
)