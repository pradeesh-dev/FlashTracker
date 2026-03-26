package com.devx.flashtrack.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.devx.flashtrack.ui.theme.*
import kotlinx.coroutines.launch

data class OnboardingPage(val emoji: String, val title: String, val subtitle: String, val accent: Color)

val onboardingPages = listOf(
    OnboardingPage("⚡", "FlashTrack", "Your privacy-first expense tracker.\n100% offline. No sign-up. No ads.", GreenIncome),
    OnboardingPage("💰", "Track Everything", "Expenses, income, transfers,\ndebts & IOUs — all in one place.", BlueCard),
    OnboardingPage("🔒", "100% Private", "All data stays on your device.\nEncrypted. Secure. Always yours.", PurpleDebt)
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val p = onboardingPages[page]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Big emoji with glow circle
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(p.accent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = p.emoji, fontSize = 64.sp)
                }

                Spacer(Modifier.height(48.dp))

                Text(
                    text = p.title,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = p.subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = DarkTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp
                )
            }
        }

        // Bottom controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(onboardingPages.size) { i ->
                    Box(
                        modifier = Modifier
                            .animateContentSize()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (pagerState.currentPage == i) GreenIncome
                                else DarkTextTertiary
                            )
                            .width(if (pagerState.currentPage == i) 24.dp else 6.dp)
                    )
                }
            }

            // Buttons
            if (pagerState.currentPage < onboardingPages.size - 1) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = onFinish) {
                        Text("Skip", color = DarkTextSecondary)
                    }
                    Button(
                        onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenIncome),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Next", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Button(
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenIncome),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Get Started 🚀", color = Color.Black, fontWeight = FontWeight.Bold,
                         style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
