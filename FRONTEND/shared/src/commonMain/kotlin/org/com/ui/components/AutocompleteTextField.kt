package org.com.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.com.ui.PlaceSuggestionBundle
import org.com.ui.PlaceSuggestionEngine
import org.com.ui.PlaceSuggestionItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutocompleteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<String>,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.LocationOn,
    onSuggestionSelected: (String) -> Unit = {},
    onPlaceSelected: ((PlaceSuggestionItem) -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) }
    var dismissedForValue by remember { mutableStateOf<String?>(null) }

    val bundle: PlaceSuggestionBundle = remember(value, suggestions) {
        PlaceSuggestionEngine.suggest(value, suggestions)
    }

    val showDropdown = isFocused &&
        dismissedForValue != value &&
        (bundle.primary.isNotEmpty() || bundle.related.isNotEmpty())

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = { next ->
                dismissedForValue = null
                onValueChange(next)
                isFocused = true
            },
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                },
            shape = RoundedCornerShape(14.dp),
            leadingIcon = { Icon(icon, null, tint = Color(0xFF1A237E)) },
            trailingIcon = {
                if (value.isNotEmpty()) {
                    IconButton(onClick = {
                        dismissedForValue = null
                        onValueChange("")
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear location",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1A237E),
                unfocusedBorderColor = Color.LightGray,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        if (showDropdown) {
            Spacer(Modifier.height(4.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, Color(0xFFD5E0F0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 4.dp)
                ) {
                    if (bundle.primary.isNotEmpty()) {
                        SectionHeaderRow(
                            title = if (bundle.isPopularMode) "POPULAR LOCATIONS IN TANZANIA" else "MATCHING LOCATIONS",
                            accentColor = Color(0xFF475569),
                            bgColor = Color(0xFFF8FAFC)
                        )
                        bundle.primary.forEach { item ->
                            LocationSuggestionRow(
                                item = item,
                                query = value,
                                icon = icon,
                                iconTint = Color(0xFF142170),
                                badgeBg = Color(0xFFEEF2FF),
                                badgeTextColor = Color(0xFF142170),
                                onClick = {
                                    val formatted = formatSelectedPlace(item)
                                    dismissedForValue = formatted
                                    isFocused = false
                                    onSuggestionSelected(formatted)
                                    onPlaceSelected?.invoke(item)
                                }
                            )
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                        }
                    }

                    if (bundle.related.isNotEmpty()) {
                        SectionHeaderRow(
                            title = "RELATED & NEARBY AREAS",
                            accentColor = Color(0xFF92400E),
                            bgColor = Color(0xFFFFFBEB)
                        )
                        bundle.related.forEach { item ->
                            LocationSuggestionRow(
                                item = item,
                                query = value,
                                icon = icon,
                                iconTint = Color(0xFFD97706),
                                badgeBg = Color(0xFFFEF3C7),
                                badgeTextColor = Color(0xFF92400E),
                                onClick = {
                                    val formatted = formatSelectedPlace(item)
                                    dismissedForValue = formatted
                                    isFocused = false
                                    onSuggestionSelected(formatted)
                                    onPlaceSelected?.invoke(item)
                                }
                            )
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                        }
                    }
                }
            }
        }
    }
}

private fun formatSelectedPlace(item: PlaceSuggestionItem): String {
    val sub = item.subtitle.trim()
    return if (item.hasCoordinates && sub.isNotBlank() && !sub.contains("km from") && !item.name.contains(",")) {
        "${item.name}, ${sub.substringAfterLast(",").trim().ifBlank { sub }}"
    } else {
        item.name
    }
}

@Composable
private fun SectionHeaderRow(
    title: String,
    accentColor: Color,
    bgColor: Color
) {
    Text(
        text = title,
        fontSize = 10.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.6.sp,
        color = accentColor,
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    )
}

@Composable
private fun LocationSuggestionRow(
    item: PlaceSuggestionItem,
    query: String,
    icon: ImageVector,
    iconTint: Color,
    badgeBg: Color,
    badgeTextColor: Color,
    onClick: () -> Unit
) {
    val highlightedTitle = remember(item.name, query) {
        buildHighlightedText(item.name, query)
    }

    val badgeLabel = when {
        item.isRelated -> "Nearby"
        item.category == "district" -> "District"
        item.category == "university" -> "Campus"
        item.category == "landmark" -> "Landmark"
        item.category == "city" -> "City"
        else -> null
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = RoundedCornerShape(9.dp),
            color = badgeBg
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp),
                    tint = iconTint
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = highlightedTitle,
                fontSize = 13.5.sp,
                color = Color(0xFF0F172A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (item.subtitle.isNotBlank()) {
                Text(
                    text = item.subtitle,
                    fontSize = 11.5.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (badgeLabel != null) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = badgeBg
            ) {
                Text(
                    text = badgeLabel,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeTextColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

private fun buildHighlightedText(text: String, rawQuery: String) = buildAnnotatedString {
    val q = rawQuery.trim()
    if (q.isEmpty()) {
        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(text) }
        return@buildAnnotatedString
    }
    val lowerText = text.lowercase()
    val lowerQ = q.lowercase()
    val startIdx = lowerText.indexOf(lowerQ)
    if (startIdx >= 0) {
        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
            append(text.substring(0, startIdx))
        }
        withStyle(
            SpanStyle(
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF142170),
                background = Color(0xFFE0E7FF)
            )
        ) {
            append(text.substring(startIdx, startIdx + q.length))
        }
        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
            append(text.substring(startIdx + q.length))
        }
    } else {
        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(text) }
    }
}
