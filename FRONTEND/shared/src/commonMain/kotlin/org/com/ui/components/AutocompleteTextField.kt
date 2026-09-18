package org.com.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

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
    onSuggestionSelected: (String) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    val filteredSuggestions = remember(value, suggestions) {
        if (value.length < 2) emptyList()
        else suggestions.filter { it.contains(value, ignoreCase = true) && it != value }.take(5)
    }

    LaunchedEffect(filteredSuggestions) {
        expanded = filteredSuggestions.isNotEmpty()
    }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = true
            },
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            leadingIcon = { Icon(icon, null, tint = Color(0xFF1A237E)) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1A237E),
                unfocusedBorderColor = Color.LightGray
            )
        )

        if (expanded && filteredSuggestions.isNotEmpty()) {
            Popup(
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = false)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 64.dp), // Position below the TextField
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                ) {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(filteredSuggestions) { suggestion ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSuggestionSelected(suggestion)
                                        expanded = false
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(icon, null, modifier = Modifier.size(18.dp), tint = Color.Gray)
                                Spacer(Modifier.width(12.dp))
                                Text(suggestion, fontSize = 14.sp)
                            }
                            HorizontalDivider(color = Color.FaintGray())
                        }
                    }
                }
            }
        }
    }
}

private fun Color.Companion.FaintGray() = Color.LightGray.copy(alpha = 0.2f)
