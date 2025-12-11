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
    androidx.compose.ui.text.font.Font(R.font.amatic_sc_regular),
)

// Set of Material typography styles to start with
val Typography = Typography(
    bodySmall = TextStyle(
        fontFamily = fontFamilyHandwriting,// FontFamily.Default,
    ),
    bodyMedium = TextStyle(
        fontFamily = fontFamilyHandwriting,// FontFamily.Default,
    ),
    bodyLarge = TextStyle(
        fontFamily = fontFamilyHandwriting,// FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = fontFamilyHandwriting,// FontFamily.Default,
    ),
    displayMedium = TextStyle(
        fontFamily = fontFamilyHandwriting,// FontFamily.Default,
    ),
    titleMedium = TextStyle(
        fontFamily = fontFamilyHandwriting,// FontFamily.Default,
    ),
    titleLarge = TextStyle(
        fontFamily = fontFamilyHandwriting,// FontFamily.Default,
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