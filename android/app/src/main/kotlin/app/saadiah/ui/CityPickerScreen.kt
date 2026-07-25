package app.saadiah.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import app.saadiah.model.City

@Composable
fun CityPickerScreen(
    selected: City,
    onPick: (City) -> Unit,
    onCancel: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val results = remember(query) { searchCities(query) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.padding(horizontal = ScreenPadding, vertical = SectionGap)) {
            PickerHeader(selectedName = selected.name, onCancel = onCancel)
            Spacer(Modifier.height(RowGap))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text(text = "Search for your city", fontSize = BodySize) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(RowGap))
            CityResults(results = results, selected = selected, onPick = onPick)
        }
    }
}

@Composable
private fun PickerHeader(
    selectedName: String,
    onCancel: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Choose your city",
            fontSize = HeroNameSize,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Caption("Prayer times are calculated for this location. Currently $selectedName.")
        TextButton(
            onClick = onCancel,
            modifier = Modifier.heightIn(min = MinimumTapTarget),
        ) {
            Text(text = "Keep $selectedName", fontSize = BodySize)
        }
    }
}

@Composable
private fun CityResults(
    results: List<City>,
    selected: City,
    onPick: (City) -> Unit,
) {
    if (results.isEmpty()) {
        Body("No city matches that name yet. The full database arrives with the offline city pack.")
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(results) { city ->
            CityRow(city = city, isSelected = city.id == selected.id, onPick = onPick)
            HorizontalDivider()
        }
    }
}

@Composable
private fun CityRow(
    city: City,
    isSelected: Boolean,
    onPick: (City) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = MinimumTapTarget)
                .clickable { onPick(city) }
                .padding(vertical = RowGap / 2),
    ) {
        Text(
            text = if (isSelected) "${city.name} — selected" else city.name,
            fontSize = BodySize,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Caption("${city.admin1}, ${city.country.value}")
    }
}
