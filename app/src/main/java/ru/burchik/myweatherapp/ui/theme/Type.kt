package ru.burchik.myweatherapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import ru.burchik.myweatherapp.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val fontName = GoogleFont("Amatic SC")

val fontFamilyHandwriting = FontFamily(
    Font(googleFont = fontName, fontProvider = provider),
    androidx.compose.ui.text.font.Font(R.font.amatic_sc_bold),
)

// Set of Material typography styles to start with
val Typography = Typography(
    bodySmall = TextStyle(
        fontFamily = fontFamilyHandwriting,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
    ),
    bodyMedium = TextStyle(
        fontFamily = fontFamilyHandwriting,
        fontSize = 16.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = fontFamilyHandwriting,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = fontFamilyHandwriting,
        fontSize = 24.sp,
    ),
    displayMedium = TextStyle(
        fontFamily = fontFamilyHandwriting,
        fontSize = 24.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = fontFamilyHandwriting,
        fontSize = 24.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = fontFamilyHandwriting,
        fontSize = 28.sp,
    ),

    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)