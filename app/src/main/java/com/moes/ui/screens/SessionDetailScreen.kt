package com.moes.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moes.ui.composables.SessionRouteMap
import com.moes.ui.viewmodels.SessionDetailViewModel
import com.moes.ui.viewmodels.ViewModelFactory
import com.moes.utils.CaloriesCalculator
import com.moes.utils.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    sessionId: String,
    onNavigateBack: () -> Unit,
    viewModel: SessionDetailViewModel = viewModel(factory = ViewModelFactory(LocalContext.current))
) {
    LaunchedEffect(sessionId) {
        viewModel.loadSession(sessionId)
    }

    val session by viewModel.session.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val focusManager = LocalFocusManager.current

    var titleText by remember { mutableStateOf("") }
    var isInitialized by remember { mutableStateOf(false) }
    var showCalorieInfoDialog by remember { mutableStateOf(false) }

    LaunchedEffect(session) {
        if (!isInitialized && session != null) {
            titleText = session!!.title
            isInitialized = true
        }
    }

    // Dialog Informativo Calorie
    if (showCalorieInfoDialog) {
        AlertDialog(
            onDismissRequest = { showCalorieInfoDialog = false },
            containerColor = Color.White,  // ← SFONDO BIANCO
            icon = { Icon(Icons.Default.Info, contentDescription = null) },
            title = { Text("Calcolo Calorie") },
            text = {
                Text(
                    "Le calorie mostrate sono una stima basata sul tipo di attività, la durata e la distanza.\n\n" +
                            "Per migliorare la precisione, assicurati di aver inserito Peso, Altezza e Data di Nascita aggiornati nel tuo Profilo."
                )
            },
            confirmButton = {
                TextButton(onClick = { showCalorieInfoDialog = false }) {
                    Text("Ho capito")
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Dettaglio Allenamento",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        if (session != null) {
                            Icon(
                                imageVector = if (session!!.isSynced) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                contentDescription = "Sync Status",
                                tint = if (session!!.isSynced) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        if (session == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val s = session!!
            val scrollState = rememberScrollState()
            val horizontalPadding = 24.dp

            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // MAPPA o PLACEHOLDER
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .padding(horizontal = horizontalPadding)
                ) {
                    if (s.routeGeometry.isNotEmpty()) {
                        SessionRouteMap(
                            encodedGeometry = s.routeGeometry,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Nessun percorso registrato",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Non ti sei spostato abbastanza per tracciare una mappa.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(horizontal = horizontalPadding, vertical = 24.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // TITOLO
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 2.dp,
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 16.dp, horizontal = 20.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "NOME ALLENAMENTO",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = titleText,
                                onValueChange = { titleText = it },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = { focusManager.clearFocus() }
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                textStyle = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // DATA E ORARIO
                    FullWidthStatCard(
                        label = "DATA",
                        mainText = FormatUtils.formatDate(s.startTime),
                        subText = FormatUtils.formatTimeRange(s.startTime, s.endTime)
                    )

                    // STATISTICHE
                    StatRow(
                        label1 = "Distanza Totale",
                        value1 = FormatUtils.formatDistance(s.distanceMeters),
                        label2 = "Tempo Totale",
                        value2 = FormatUtils.formatDuration(s.durationMs)
                    )
                    StatRow(
                        label1 = "Velocità Media",
                        value1 = FormatUtils.formatSpeed(s.avgSpeedKmh),
                        label2 = "Passo Medio",
                        value2 = FormatUtils.formatPace(s.avgPaceSeconds)
                    )

                    // CALORIE
                    if (userProfile != null) {
                        val kCal = CaloriesCalculator.calculate(s, userProfile!!)
                        CalorieStatCard(
                            kcal = kCal,
                            onInfoClick = { showCalorieInfoDialog = true }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // BOTTONE SALVA
                    val hasChanges = titleText.trim() != s.title
                    Button(
                        onClick = { viewModel.saveTitle(titleText) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = hasChanges,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = 0.5f
                            )
                        ),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Salva Modifiche", fontWeight = FontWeight.Bold)
                    }

                    // BOTTONE ELIMINA
                    Button(
                        onClick = { viewModel.deleteSession { onNavigateBack() } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Elimina Allenamento", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// COMPONENTI DI SUPPORTO
@Composable
fun FullWidthStatCard(label: String, mainText: String, subText: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label.uppercase(), style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp
                ), color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = mainText, style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold, fontSize = 24.sp
                ), color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subText, style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ), color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CalorieStatCard(kcal: String, onInfoClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .padding(vertical = 16.dp, horizontal = 20.dp)
                    .align(Alignment.CenterStart),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "CALORIE STIMATE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp
                    ), color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = kcal, style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold, fontSize = 24.sp
                    ), color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Kcal", style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ), color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onInfoClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info Calorie",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}


@Composable
fun StatRow(label1: String, value1: String, label2: String, value2: String) {
    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatCard(label1, value1, Modifier.weight(1f))
        StatCard(label2, value2, Modifier.weight(1f))
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label.uppercase(), style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp
                ), color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value, style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold, fontSize = 22.sp
                ), color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}