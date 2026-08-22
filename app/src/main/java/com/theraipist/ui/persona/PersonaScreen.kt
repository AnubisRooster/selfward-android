package com.theraipist.ui.persona

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.theraipist.config.CompanionGender
import com.theraipist.config.CompanionPersonality
import com.theraipist.config.PersonaKind
import com.theraipist.config.SpiritualTradition

@Composable
fun PersonaScreen(viewModel: PersonaViewModel = hiltViewModel()) {
    val persona by viewModel.persona.collectAsState()
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Companion", style = MaterialTheme.typography.headlineSmall)
        Text("Current: ${persona.kind} · ${persona.companionGender} · ${persona.companionPersonality} · ${persona.spiritualTradition}")

        EnumSelector("Kind", enumValues<PersonaKind>(), persona.kind) { viewModel.setKind(it) }
        EnumSelector("Gender", enumValues<CompanionGender>(), persona.companionGender) { viewModel.setGender(CompanionGender.valueOf(it.name)) }
        EnumSelector("Personality", enumValues<CompanionPersonality>(), persona.companionPersonality) { viewModel.setPersonality(CompanionPersonality.valueOf(it.name)) }
        EnumSelector("Tradition", enumValues<SpiritualTradition>(), persona.spiritualTradition) { viewModel.setSpiritualTradition(SpiritualTradition.valueOf(it.name)) }
    }
}

@Composable
private inline fun <reified T : Enum<T>> EnumSelector(
    label: String,
    values: Array<T>,
    selected: T,
    crossinline onSelect: (T) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.titleSmall)
        androidx.compose.foundation.layout.Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            values.forEach { value ->
                val isSel = value == selected
                Button(
                    onClick = { onSelect(value) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(value.name)
                }
            }
        }
    }
}
