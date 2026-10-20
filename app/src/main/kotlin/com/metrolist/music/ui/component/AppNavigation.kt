/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.component
import com.metrolist.music.ui.utils.appleFrosted
import com.metrolist.music.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.metrolist.music.ui.screens.Screens
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Stable
private fun isRouteSelected(currentRoute: String?, screenRoute: String, navigationItems: List<Screens>): Boolean {
    if (currentRoute == null) return false
    if (currentRoute == screenRoute) return true
    if (navigationItems.any { it.route == screenRoute } &&
        currentRoute.startsWith("$screenRoute/")) return true

    // Fix: match the route template, not the resolved route
    if (screenRoute == "search_input" &&
        (currentRoute.startsWith("search/") || currentRoute == "search/{query}")) return true

    return false
}

@Composable
fun AppNavigationRail(
    navigationItems: List<Screens>,
    currentRoute: String?,
    onItemClick: (Screens, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    pureBlack: Boolean = false,
    onSearchLongClick: (() -> Unit)? = null,
    onHomeLongHold: (() -> Unit)? = null,
) {
    val containerColor = if (pureBlack) Color.Black else MaterialTheme.colorScheme.surfaceContainer
    val haptics = LocalHapticFeedback.current
    val viewConfiguration = LocalViewConfiguration.current

    NavigationRail(
        modifier = modifier,
        containerColor = containerColor
    ) {
        Spacer(modifier = Modifier.weight(1f))

        navigationItems.forEach { screen ->
            val isSelected = remember(currentRoute, screen.route) {
                isRouteSelected(currentRoute, screen.route, navigationItems)
            }
            val currentIsSelected by rememberUpdatedState(isSelected)
            val iconRes = remember(isSelected, screen) {
                if (isSelected) screen.iconIdActive else screen.iconIdInactive
            }

            val isSearchItem = screen == Screens.Search && onSearchLongClick != null
            val isHomeHoldItem = screen == Screens.Home && onHomeLongHold != null
            val interactionSource = remember { MutableInteractionSource() }

            // Long press detection using InteractionSource
            if (isSearchItem || isHomeHoldItem) {
                LaunchedEffect(interactionSource) {
                    var isLongClick = false
                    interactionSource.interactions.collectLatest { interaction ->
                        when (interaction) {
                            is PressInteraction.Press -> {
                                isLongClick = false
                                delay(if (isHomeHoldItem) 15_000L else viewConfiguration.longPressTimeoutMillis)
                                isLongClick = true
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (isHomeHoldItem) onHomeLongHold.invoke() else onSearchLongClick?.invoke()
                            }
                            is PressInteraction.Release -> {
                                if (!isLongClick) {
                                    onItemClick(screen, currentIsSelected)
                                }
                            }
                            is PressInteraction.Cancel -> {
                                isLongClick = false
                            }
                        }
                    }
                }
            }

            NavigationRailItem(
                selected = isSelected,
                onClick = {
                    if (!isSearchItem && !isHomeHoldItem) {
                        onItemClick(screen, currentIsSelected)
                    }
                    // Long presses are handled via InteractionSource
                },
                interactionSource = interactionSource,
                icon = {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = stringResource(screen.titleId)
                    )
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun AppNavigationBar(
    navigationItems: List<Screens>,
    currentRoute: String?,
    onItemClick: (Screens, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    pureBlack: Boolean = false,
    slimNav: Boolean = false,
    onSearchLongClick: (() -> Unit)? = null,
    onHomeLongHold: (() -> Unit)? = null,
) {
    val mainItems = navigationItems.filter { it != Screens.Search }
    val searchScreen = navigationItems.firstOrNull { it == Screens.Search }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .height(64.dp)
                .clip(CircleShape)
                .appleFrosted()
                .border(0.5.dp, AppleNavOutline, CircleShape)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            mainItems.forEach { screen ->
                val isSelected = remember(currentRoute, screen.route) {
                    isRouteSelected(currentRoute, screen.route, navigationItems)
                }
                AppleNavItem(
                    screen = screen,
                    isSelected = isSelected,
                    showLabel = !slimNav,
                    circle = false,
                    onItemClick = onItemClick,
                    onSearchLongClick = onSearchLongClick,
                    onHomeLongHold = onHomeLongHold,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }
        }
        if (searchScreen != null) {
            val isSelected = remember(currentRoute, searchScreen.route) {
                isRouteSelected(currentRoute, searchScreen.route, navigationItems)
            }
            AppleNavItem(
                screen = searchScreen,
                isSelected = isSelected,
                showLabel = false,
                circle = true,
                onItemClick = onItemClick,
                onSearchLongClick = onSearchLongClick,
                onHomeLongHold = onHomeLongHold,
                modifier = Modifier.size(64.dp),
            )
        }
    }
}

@Composable
private fun AppleNavItem(
    screen: Screens,
    isSelected: Boolean,
    showLabel: Boolean,
    circle: Boolean,
    onItemClick: (Screens, Boolean) -> Unit,
    onSearchLongClick: (() -> Unit)?,
    onHomeLongHold: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val viewConfiguration = LocalViewConfiguration.current
    val currentIsSelected by rememberUpdatedState(isSelected)
    val iconRes =
        when (screen) {
            Screens.Home -> R.drawable.apple_home
            Screens.Search -> R.drawable.apple_search
            else -> if (isSelected) screen.iconIdActive else screen.iconIdInactive
        }
    val isSearchItem = screen == Screens.Search && onSearchLongClick != null
    val isHomeHoldItem = screen == Screens.Home && onHomeLongHold != null
    val interactionSource = remember { MutableInteractionSource() }

    if (isSearchItem || isHomeHoldItem) {
        LaunchedEffect(interactionSource) {
            var isLongClick = false
            interactionSource.interactions.collectLatest { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> {
                        isLongClick = false
                        delay(if (isHomeHoldItem) 15_000L else viewConfiguration.longPressTimeoutMillis)
                        isLongClick = true
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (isHomeHoldItem) onHomeLongHold?.invoke() else onSearchLongClick?.invoke()
                    }
                    is PressInteraction.Release -> {
                        if (!isLongClick) {
                            onItemClick(screen, currentIsSelected)
                        }
                    }
                    is PressInteraction.Cancel -> {
                        isLongClick = false
                    }
                }
            }
        }
    }

    val tint = if (isSelected) AppleNavRed else AppleNavGray
    val bg = when {
        circle -> Color.Transparent
        isSelected -> Color.White.copy(alpha = 0.12f)
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .then(if (circle) Modifier.appleFrosted() else Modifier.background(bg))
            .then(if (circle) Modifier.border(0.5.dp, AppleNavOutline, CircleShape) else Modifier)
            .clickable(interactionSource = interactionSource, indication = null) {
                if (!isSearchItem && !isHomeHoldItem) {
                    onItemClick(screen, currentIsSelected)
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = stringResource(screen.titleId),
                tint = tint,
                modifier = Modifier.size(if (circle) 26.dp else 24.dp),
            )
            if (showLabel && !circle) {
                Text(
                    text = stringResource(screen.titleId),
                    color = tint,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private val AppleNavRed = Color(0xFFFA2D48)
private val AppleNavGray = Color(0xFF8E8E93)
private val AppleNavSurface = Color(0xFF1C1C1E)
private val AppleNavOutline = Color(0xFF38383A)
