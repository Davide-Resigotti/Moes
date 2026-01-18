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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moes.ui.composables.SessionRouteMap
import com.moes.ui.viewmodels.SessionDetailViewModel
import com.moes.ui.viewmodels.ViewModelFactory
import com.moes.utils.CaloriesCalculator
import com.moes.utils.FormatUtils
import kotlinx.coroutines.delay

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

    var titleText by remember { mutableStateOf("") }
    var isInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(session) {
        if (!isInitialized && session != null) {
            titleText = session!!.title
            isInitialized = true
        }
    }

    LaunchedEffect(titleText) {
        if (isInitialized && session != null) {
            delay(1000)
            if (titleText.trim() != session!!.title) {
                viewModel.updateTitleSilently(titleText)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background, topBar = {
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
            }, navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
            )
        }) { padding ->
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
                // MAPPA
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .padding(horizontal = horizontalPadding)
                ) {
                    SessionRouteMap(
                        encodedGeometry = s.routeGeometry, modifier = Modifier.fillMaxSize()
                    )
                }

                Column(
                    modifier = Modifier
                        .padding(horizontal = horizontalPadding, vertical = 24.dp)
                        .fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // TEXTFIELD TITLE
                    Surface(
                        shape = CircleShape,
                        shadowElevation = 2.dp,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                        ) {
                            TextField(
                                value = titleText,
                                onValueChange = { titleText = it },
                                placeholder = {
                                    Text(
                                        "Nome Allenamento",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                ),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.primary
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

                    if (userProfile != null) {
                        val kCal = CaloriesCalculator.calculate(s, userProfile!!)
                        FullWidthStatCard(
                            label = "Calorie", mainText = kCal, subText = "Kcal"
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

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