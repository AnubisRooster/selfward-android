package com.theraipist.ui.persona

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
import com.theraipist.ui.components.SelectionChips
import com.theraipist.ui.components.prettifyEnumName

@Composable
fun PersonaScreen(viewModel: PersonaViewModel = hiltViewModel()) {
    val persona by viewModel.persona.collectAsState()
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Companion", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Currently: ${prettifyEnumName(persona.kind.name)} · ${persona.companionGender.label} · " +
                "${persona.companionPersonality.label} · ${persona.spiritualTradition.label}",
            style = MaterialTheme.typography.bodyMedium
        )

        Section("Kind") {
            SelectionChips(
                options = PersonaKind.entries,
                selected = persona.kind,
                label = { prettifyEnumName(it.name) },
                onSelect = { viewModel.setKind(it) }
            )
        }
        Section("Gender") {
            SelectionChips(
                options = CompanionGender.entries,
                selected = persona.companionGender,
                label = { it.label },
                onSelect = { viewModel.setGender(it) }
            )
        }
        Section("Personality") {
            SelectionChips(
                options = CompanionPersonality.entries,
                selected = persona.companionPersonality,
                label = { it.label },
                onSelect = { viewModel.setPersonality(it) }
            )
        }
        Section("Tradition") {
            SelectionChips(
                options = SpiritualTradition.entries,
                selected = persona.spiritualTradition,
                label = { it.label },
                onSelect = { viewModel.setSpiritualTradition(it) }
            )
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        content()
    }
}
