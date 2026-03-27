package com.devx.flashtrack.ui.screens.onboarding

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devx.flashtrack.ui.theme.*
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val accent: Color
)

private val pages = listOf(
    OnboardingPage("⚡", "FlashTrack",       "Your privacy-first expense tracker.\n100% offline. No sign-up. No ads.",    GreenIncome),
    OnboardingPage("💰", "Track Everything", "Expenses, income, transfers,\ndebts & IOUs — all in one place.",          BlueCard),
    OnboardingPage("🔒", "100% Private",     "All data stays on your device.\nEncrypted. Secure. Always yours.",         PurpleDebt)
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope      = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            val p = pages[page]
            Column(
                modifier = Modifier.fillMaxSize().padding(36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(140.dp).clip(CircleShape).background(p.accent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) { Text(p.emoji, fontSize = 64.sp) }

                Spacer(Modifier.height(52.dp))

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
                .padding(horizontal = 32.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Dot indicators
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(pages.size) { i ->
                    Box(
                        modifier = Modifier
                            .animateContentSize()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (pagerState.currentPage == i) GreenIncome else DarkTextTertiary)
                            .width(if (pagerState.currentPage == i) 24.dp else 6.dp)
                    )
                }
            }

            if (pagerState.currentPage < pages.size - 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onFinish) {
                        Text("Skip", color = DarkTextSecondary, style = MaterialTheme.typography.bodyMedium)
                    }
                    Button(
                        onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenIncome),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Next →", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Button(
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenIncome),
                    shape  = RoundedCornerShape(16.dp)
                ) {
                    Text("Get Started 🚀", color = Color.Black, fontWeight = FontWeight.Bold,
                         style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
