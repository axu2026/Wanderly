package com.example.wanderly.ui.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Museum
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val icon: ImageVector,
    val accents: List<ImageVector>,
    val title: String,
    val body: String,
    val accent: AccentRole,
)

private enum class AccentRole { PRIMARY, SECONDARY, TERTIARY }

private val pages = listOf(
    OnboardingPage(
        icon = Icons.Outlined.Explore,
        accents = listOf(Icons.Outlined.LocationOn, Icons.Outlined.Place, Icons.Outlined.Star),
        title = "Discover places near you",
        body = "Wanderly suggests highly-rated spots around your current location, with photos, weather, and the story behind each place.",
        accent = AccentRole.PRIMARY,
    ),
    OnboardingPage(
        icon = Icons.Outlined.CalendarMonth,
        accents = listOf(Icons.Outlined.Restaurant, Icons.Outlined.Museum, Icons.Outlined.Star),
        title = "Plan a day, not just a stop",
        body = "Tell us your interests, budget, and pace. We'll build a balanced day-by-day itinerary you can save and revisit.",
        accent = AccentRole.SECONDARY,
    ),
    OnboardingPage(
        icon = Icons.Outlined.Navigation,
        accents = listOf(Icons.Outlined.Route, Icons.Outlined.Map, Icons.Outlined.LocationOn),
        title = "See the route — go when ready",
        body = "Routes are drawn on the map as you plan. One tap launches Google Maps when it's time to head out.",
        accent = AccentRole.TERTIARY,
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == pages.lastIndex

    Scaffold(containerColor = MaterialTheme.colorScheme.surface) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                if (!isLast) {
                    TextButton(onClick = onFinish) { Text("Skip") }
                } else {
                    Spacer(Modifier.height(48.dp))
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) { pageIndex ->
                OnboardingPageContent(pages[pageIndex])
            }

            PageIndicator(
                pageCount = pages.size,
                currentPage = pagerState.currentPage,
            )
            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            ) {
                Button(
                    onClick = {
                        if (isLast) onFinish()
                        else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                ) {
                    Text(
                        text = if (isLast) "Get started" else "Next",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    if (!isLast) {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IllustratedHero(page = page)
        Spacer(Modifier.height(32.dp))
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = page.body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 36.dp),
        )
    }
}

private data class AccentColors(
    val container: Color,
    val onContainer: Color,
    val emphasis: Color,
    val onEmphasis: Color,
)

@Composable
private fun resolveAccent(role: AccentRole): AccentColors {
    val s = MaterialTheme.colorScheme
    return when (role) {
        AccentRole.PRIMARY -> AccentColors(s.primaryContainer, s.onPrimaryContainer, s.primary, s.onPrimary)
        AccentRole.SECONDARY -> AccentColors(s.secondaryContainer, s.onSecondaryContainer, s.secondary, s.onSecondary)
        AccentRole.TERTIARY -> AccentColors(s.tertiaryContainer, s.onTertiaryContainer, s.tertiary, s.onTertiary)
    }
}

@Composable
private fun IllustratedHero(page: OnboardingPage) {
    val colors = resolveAccent(page.accent)
    val gradient = Brush.verticalGradient(
        colors = listOf(colors.container, MaterialTheme.colorScheme.surface),
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp)
            .background(gradient),
        contentAlignment = Alignment.Center,
    ) {
        // Layered concentric circles establish depth without an external asset.
        Surface(
            shape = CircleShape,
            color = colors.emphasis.copy(alpha = 0.08f),
            modifier = Modifier.size(280.dp),
        ) {}
        Surface(
            shape = CircleShape,
            color = colors.emphasis.copy(alpha = 0.16f),
            modifier = Modifier.size(200.dp),
        ) {}
        Surface(
            shape = CircleShape,
            color = colors.emphasis,
            modifier = Modifier.size(120.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    tint = colors.onEmphasis,
                    modifier = Modifier.size(60.dp),
                )
            }
        }
        // Floating accent badges arranged around the hero.
        AccentBadge(
            icon = page.accents[0],
            colors = colors,
            offsetX = (-110).dp,
            offsetY = (-80).dp,
            size = 48.dp,
        )
        AccentBadge(
            icon = page.accents[1],
            colors = colors,
            offsetX = 120.dp,
            offsetY = (-60).dp,
            size = 44.dp,
        )
        AccentBadge(
            icon = page.accents[2],
            colors = colors,
            offsetX = 90.dp,
            offsetY = 100.dp,
            size = 40.dp,
        )
    }
}

@Composable
private fun AccentBadge(
    icon: ImageVector,
    colors: AccentColors,
    offsetX: androidx.compose.ui.unit.Dp,
    offsetY: androidx.compose.ui.unit.Dp,
    size: androidx.compose.ui.unit.Dp,
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        modifier = Modifier
            .size(size)
            .offset(x = offsetX, y = offsetY),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.emphasis,
                modifier = Modifier.size(size * 0.55f),
            )
        }
    }
}

@Composable
private fun PageIndicator(pageCount: Int, currentPage: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage
            val width by animateDpAsState(
                targetValue = if (isActive) 28.dp else 8.dp,
                label = "indicator-width",
            )
            val color by animateColorAsState(
                targetValue = if (isActive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceContainerHighest,
                label = "indicator-color",
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .height(8.dp)
                    .width(width)
                    .background(color = color, shape = RoundedCornerShape(4.dp)),
            )
        }
    }
}
