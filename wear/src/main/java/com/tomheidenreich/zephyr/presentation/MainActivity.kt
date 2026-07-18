package com.tomheidenreich.zephyr.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.tomheidenreich.zephyr.core.result.RepositoryResult
import com.tomheidenreich.zephyr.domain.model.LiveMetrics
import com.tomheidenreich.zephyr.domain.model.PointOfSail
import com.tomheidenreich.zephyr.domain.model.SessionState
import com.tomheidenreich.zephyr.domain.model.SurfaceSnapshot
import com.tomheidenreich.zephyr.domain.model.WindObservation
import com.tomheidenreich.zephyr.domain.repository.ExerciseSessionRepository
import com.tomheidenreich.zephyr.domain.repository.LiveMetricsRepository
import com.tomheidenreich.zephyr.domain.repository.SurfaceSnapshotRepository
import com.tomheidenreich.zephyr.domain.repository.WindRepository
import com.tomheidenreich.zephyr.domain.usecase.BuildSurfaceSnapshotUseCase
import com.tomheidenreich.zephyr.domain.usecase.DeriveSailingMetricsUseCase
import com.tomheidenreich.zephyr.presentation.theme.ZephyrTheme
import com.tomheidenreich.zephyr.runtime.di.AppGraph
import com.tomheidenreich.zephyr.runtime.di.UseCaseGraph
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

private const val DEFAULT_SPOT_ID = "home-reef"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            zephyrDashboard()
        }
    }
}

@Composable
fun zephyrDashboard(
    exerciseSessionRepository: ExerciseSessionRepository = AppGraph.exerciseSessionRepository,
    liveMetricsRepository: LiveMetricsRepository = AppGraph.liveMetricsRepository,
    windRepository: WindRepository = AppGraph.windRepository,
    surfaceSnapshotRepository: SurfaceSnapshotRepository = AppGraph.surfaceSnapshotRepository,
    deriveSailingMetricsUseCase: DeriveSailingMetricsUseCase = UseCaseGraph.deriveSailingMetricsUseCase,
    buildSurfaceSnapshotUseCase: BuildSurfaceSnapshotUseCase = UseCaseGraph.buildSurfaceSnapshotUseCase,
) {
    ZephyrTheme {
        AppScaffold {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()

            val sessionState = rememberCollectedState(exerciseSessionRepository.observeSessionState())
            val liveMetricsState = rememberCollectedState(liveMetricsRepository.observeLiveMetrics())
            val windState = rememberCollectedState(remember { windRepository.observeCurrentWind(DEFAULT_SPOT_ID) })
            val snapshotState = rememberCollectedState(surfaceSnapshotRepository.observeLatestSnapshot())

            val sessionResult = sessionState.value ?: RepositoryResult.Loading
            val liveMetricsResult = liveMetricsState.value ?: RepositoryResult.Loading
            val windResult = windState.value ?: RepositoryResult.Loading

            val session = sessionResult.dataOrNull() ?: SessionState()
            val baseMetrics = liveMetricsResult.dataOrNull() ?: LiveMetrics()
            val windObservation = windResult.dataOrNull()
            val derivedMetrics = remember(baseMetrics, windObservation) {
                deriveSailingMetricsUseCase(
                    metrics = baseMetrics,
                    windObservation = windObservation,
                )
            }
            val snapshot = snapshotState.value ?: buildSurfaceSnapshotUseCase(
                session = sessionResult,
                metrics = RepositoryResult.Data(derivedMetrics),
                wind = windResult,
            )

            ScreenScaffold(
                scrollState = listState,
                edgeButton = {
                    EdgeButton(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                    ) {
                        Text("Refresh")
                    }
                },
            ) { contentPadding ->
                TransformingLazyColumn(contentPadding = contentPadding, state = listState) {
                    item {
                        ListHeader(
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text("Zephyr")
                        }
                    }
                    item {
                        heroCard(
                            title = snapshot.headline,
                            subtitle = snapshot.subline,
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                            containerColor = cardContainerColor(isAlt = false),
                            contentColor = cardContentColor(isAlt = false),
                        )
                    }
                    item {
                        metricCard(
                            title = "Session",
                            primary = session.status.name.prettyLabel(),
                            secondary = sessionSummary(session),
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                            containerColor = cardContainerColor(isAlt = true),
                            contentColor = cardContentColor(isAlt = true),
                        )
                    }
                    item {
                        metricCard(
                            title = "Wind",
                            primary = windSummary(derivedMetrics),
                            secondary = windDetails(windObservation),
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                            containerColor = cardContainerColor(isAlt = false),
                            contentColor = cardContentColor(isAlt = false),
                        )
                    }
                    item {
                        metricCard(
                            title = "Sailing",
                            primary = pointOfSailSummary(derivedMetrics),
                            secondary = exactAngleSummary(derivedMetrics),
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                            containerColor = cardContainerColor(isAlt = true),
                            contentColor = cardContentColor(isAlt = true),
                        )
                    }
                    item {
                        metricCard(
                            title = "Performance",
                            primary = speedSummary(derivedMetrics),
                            secondary = performanceSummary(derivedMetrics),
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                            containerColor = cardContainerColor(isAlt = false),
                            contentColor = cardContentColor(isAlt = false),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun heroCard(
    title: String,
    subtitle: String,
    modifier: Modifier,
    containerColor: Color,
    contentColor: Color,
) {
    val cardShape = RoundedCornerShape(18.dp)
    Card(
        onClick = { },
        modifier = modifier.border(
            width = 1.dp,
            color = Color(0x33000000),
            shape = cardShape,
        ),
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(title)
            Text(subtitle)
        }
    }
}

@Composable
private fun metricCard(
    title: String,
    primary: String,
    secondary: String,
    modifier: Modifier,
    containerColor: Color,
    contentColor: Color,
) {
    val cardShape = RoundedCornerShape(18.dp)
    Card(
        onClick = { },
        modifier = modifier.border(
            width = 1.dp,
            color = Color(0x33000000),
            shape = cardShape,
        ),
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(title)
            Text(primary)
            Text(secondary)
        }
    }
}

@Composable
private fun <T> rememberCollectedState(flow: Flow<T>): State<T?> {
    return produceState<T?>(initialValue = null, flow) {
        flow.collectLatest { value = it }
    }
}

private fun <T> RepositoryResult<T>.dataOrNull(): T? = (this as? RepositoryResult.Data<T>)?.value

private fun sessionSummary(session: SessionState): String {
    return when {
        session.startedAt == null -> "No active session"
        session.endedAt != null -> "Ended"
        else -> "Tracking since ${session.startedAt}"
    }
}

private fun windSummary(metrics: LiveMetrics): String {
    val trueWind = metrics.trueWind?.speedKts?.let { "${"%.0f".format(it)} kt TW" } ?: "-- kt TW"
    val apparentWind = metrics.apparentWind?.speedKts?.let { "${"%.0f".format(it)} kt AW" } ?: "-- kt AW"
    return "$trueWind  ·  $apparentWind"
}

private fun windDetails(windObservation: WindObservation?): String {
    if (windObservation == null) return "Waiting for wind data"
    return "${windObservation.sourceType.name.prettyLabel()} via ${windObservation.source}"
}

private fun pointOfSailSummary(metrics: LiveMetrics): String {
    return metrics.pointOfSail?.name?.prettyLabel() ?: "Unknown"
}

private fun exactAngleSummary(metrics: LiveMetrics): String {
    return metrics.pointOfSailAngleDegrees?.let { "${"%.0f".format(it)}° off the wind" } ?: "Angle unavailable"
}

private fun speedSummary(metrics: LiveMetrics): String {
    val speed = metrics.speedMps?.let { "${"%.1f".format(it)} m/s" } ?: "-- m/s"
    val distance = "${"%.0f".format(metrics.distanceMeters)} m covered"
    return "$speed  ·  $distance"
}

private fun performanceSummary(metrics: LiveMetrics): String {
    return metrics.heartRateBpm?.let { "Heart rate ${"%.0f".format(it)} bpm" } ?: "Heart rate unavailable"
}

@Composable
private fun cardContainerColor(isAlt: Boolean): Color {
    return if (isAlt) {
        Color(0xFF2B2F34)
    } else {
        Color(0xFF23272C)
    }
}

@Composable
private fun cardContentColor(isAlt: Boolean): Color {
    return if (isAlt) {
        Color(0xFFF0F1F2)
    } else {
        Color(0xFFF6F7F8)
    }
}

private fun String.prettyLabel(): String {
    return lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun defaultPreview() {
    zephyrDashboard()
}
