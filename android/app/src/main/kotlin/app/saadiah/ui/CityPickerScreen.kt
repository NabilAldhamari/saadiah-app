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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import app.saadiah.data.CityIndex
import app.saadiah.design.MinimumTapTarget
import app.saadiah.design.SaadiahSpacing
import app.saadiah.design.SaadiahType
import app.saadiah.model.City
import app.saadiah.model.EXTENDED_CITIES
import kotlinx.datetime.TimeZone

@Composable
fun CityPickerScreen(
    selected: City,
    onPick: (City) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    // Reading seven megabytes takes long enough to see, so the field is drawn immediately
    // and the database arrives behind it rather than the screen opening late.
    val index by produceState<CityIndex?>(initialValue = null) { value = loadCityIndex(context) }
    val preferredZone = remember { runCatching { TimeZone.currentSystemDefault() }.getOrNull() }
    var query by remember { mutableStateOf("") }
    val results =
        remember(query, index, preferredZone) {
            index?.search(query, preferredTimeZone = preferredZone).orEmpty()
        }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.padding(horizontal = SaadiahSpacing.screen, vertical = SaadiahSpacing.large)) {
            ScreenHeader(title = strings.titleChooseCity, onBack = onBack)
            Caption("${strings.currentlyCity} ${selected.name}.")
            Spacer(Modifier.height(SaadiahSpacing.medium))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text(text = strings.searchForYourCity, fontSize = SaadiahType.body.size) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(SaadiahSpacing.medium))
            when {
                index == null -> Body(strings.loadingCityList)
                query.isBlank() -> {
                    Caption(strings.typeYourCity)
                    Spacer(Modifier.height(SaadiahSpacing.small))
                    CityResults(results = EXTENDED_CITIES, selected = selected, onPick = onPick)
                }
                else -> CityResults(results = results, selected = selected, onPick = onPick)
            }
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
        Body(strings.noCityMatches)
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
    val subtitle =
        if (city.admin1.isNotBlank()) {
            "${city.admin1}, ${city.country.value} (${city.timeZone.id})"
        } else {
            "${city.country.value} (${city.timeZone.id})"
        }
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = MinimumTapTarget)
                .clickable { onPick(city) }
                .padding(vertical = SaadiahSpacing.medium / 2),
    ) {
        Text(
            text = if (isSelected) "${city.name} — selected" else city.name,
            fontSize = SaadiahType.body.size,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Caption(subtitle)
    }
}
