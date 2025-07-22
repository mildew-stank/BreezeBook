package com.bsi.breezeplot.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bsi.breezeplot.viewmodels.LogEntry
import com.bsi.breezeplot.utilities.distanceToNauticalMiles
import com.bsi.breezeplot.utilities.doubleToDMS
import com.bsi.breezeplot.utilities.speedToKnots
import java.util.Locale

@Composable
fun TitleCard(
    title: String,
    data: String,
    modifier: Modifier = Modifier,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    titleBackgroundColor: Color = MaterialTheme.colorScheme.background,
    titleTextColor: Color = MaterialTheme.colorScheme.onBackground,
    dataColor: Color = MaterialTheme.colorScheme.primary,
    dataStyle: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    TitleCard(
        title = title,
        data = buildAnnotatedString { append(data) },
        modifier = modifier,
        borderColor = borderColor,
        titleBackgroundColor = titleBackgroundColor,
        titleTextColor = titleTextColor,
        dataColor = dataColor,
        dataStyle = dataStyle
    )
}

@Composable
fun TitleCard(
    title: String,
    data: AnnotatedString,
    modifier: Modifier = Modifier,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    titleBackgroundColor: Color = MaterialTheme.colorScheme.background,
    titleTextColor: Color = MaterialTheme.colorScheme.onBackground,
    dataColor: Color = MaterialTheme.colorScheme.primary,
    dataStyle: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    var titleHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    Box(modifier = modifier) {
        // Body
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = titleHeight / 2) // Push top of Box down by half title height for title alignment
                .background(MaterialTheme.colorScheme.background)
                .border(BorderStroke(2.dp, borderColor), shape = RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Text(
                text = data,
                textAlign = TextAlign.Center,
                color = dataColor,
                style = dataStyle,
            )
        }
        // Title
        Text(
            text = title,
            textAlign = TextAlign.Center,
            color = titleTextColor,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .onGloballyPositioned { coordinates ->
                    titleHeight = with(density) { coordinates.size.height.toDp() }
                }
                .background(titleBackgroundColor)
                .padding(horizontal = 8.dp))
    }
}

@Composable
fun TitledBorder(
    modifier: Modifier = Modifier,
    title: String,
    titleColor: Color = MaterialTheme.colorScheme.onBackground,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    content: @Composable () -> Unit
) {
    var titleHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    Box(modifier = modifier) {
        // Body
        Box(
            //contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = titleHeight / 2) // Push top of Box down by half title height for title alignment
                .background(backgroundColor)
                .border(BorderStroke(2.dp, borderColor), shape = RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            content()
        }
        // Title
        Text(modifier = Modifier
            .align(Alignment.TopCenter)
            .onGloballyPositioned { coordinates ->
                titleHeight = with(density) { coordinates.size.height.toDp() }
            }
            .background(backgroundColor)
            .padding(horizontal = 8.dp),
            text = title,
            textAlign = TextAlign.Center,
            color = titleColor,
            style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
fun LogEntryCard(modifier: Modifier = Modifier, entry: LogEntry) {
    val locale = Locale.getDefault()
    val latitude = remember(entry.latitude) { doubleToDMS(entry.latitude, true) }
    val longitude = remember(entry.longitude) { doubleToDMS(entry.longitude, false) }
    val speed = remember(entry.speed) { String.format(locale, "%.1fkn", speedToKnots(entry.speed)) }
    val bearing = remember(entry.bearing) { String.format(locale, "%.1f°", entry.bearing) }
    val distance = remember(entry.distance) {
        String.format(locale, "%.2fNM", distanceToNauticalMiles(entry.distance))
    }
    val pad = 12.dp

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(pad),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
    ) {
        Surface(
            shape = RoundedCornerShape(pad),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(pad),
                ambientColor = Color.Black,
                spotColor = Color.Black
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(pad),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.date,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = entry.time,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(pad),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier) {
                Text(
                    text = latitude,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = longitude,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            VerticalDivider()
            Column(modifier = Modifier) {
                Text(
                    text = speed,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = bearing,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            VerticalDivider()
            Column(modifier = Modifier) {
                Text(
                    text = distance,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
