package com.example.moodtracker.presentation.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moodtracker.MoodPulseApp
import com.example.moodtracker.domain.model.MoodOption
import com.example.moodtracker.domain.model.MoodOptions
import com.example.moodtracker.domain.model.MoodSlot
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun HomeRoute(
    contentPadding: PaddingValues,
) {
    val context = LocalContext.current
    val graph = (context.applicationContext as MoodPulseApp).appGraph

    val vm: HomeViewModel = viewModel(
        factory = SimpleVmFactory {
            HomeViewModel(
                clock = graph.clock,
                observeToday = graph.observeTodayUseCase,
                saveMood = graph.saveMoodUseCase,
                resolveOption = { id -> MoodOptions.byId(id) },
            )
        },
    )

    val ui by vm.uiState.collectAsStateWithLifecycle()
    val event by vm.event.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(event) {
        when (val e = event) {
            is HomeEvent.ShowMessage -> snackbarHostState.showSnackbar(e.message)
            HomeEvent.Saved -> snackbarHostState.showSnackbar("Vibe Locked In!")
            null -> Unit
        }
        if (event != null) vm.consumeEvent()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF030712) // Darker, cleaner background
    ) {
        // Subtle background glow
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF1E3A8A).copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(size.width * 0.5f, size.height * 0.4f),
                        radius = size.width
                    )
                )
            }

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                containerColor = Color.Transparent
            ) { inner ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(inner)
                        .padding(contentPadding)
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "VIBE BOWL",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF60A5FA),
                            letterSpacing = 4.sp
                        ),
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    Text(
                        text = "How are you feeling?",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = (-1).sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    SlotSelector(
                        selected = ui.selectedSlot,
                        todayBySlot = ui.todayBySlot,
                        onSelect = vm::selectSlot,
                    )

                    Spacer(modifier = Modifier.weight(0.7f))

                    MoodBowl(
                        options = MoodOptions.all,
                        onMoodSelected = { vm.save(it, allowReplace = true) }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    TodaySummarySection(todayBySlot = ui.todayBySlot)
                }
            }
        }
    }
}

@Composable
private fun SlotSelector(
    selected: MoodSlot,
    todayBySlot: Map<MoodSlot, MoodOption?>,
    onSelect: (MoodSlot) -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.03f))
            .border(1.dp, Color.White.copy(alpha = 0.05f), CircleShape)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MoodSlot.entries.forEach { slot ->
            val isSelected = selected == slot
            Surface(
                onClick = { onSelect(slot) },
                shape = CircleShape,
                color = if (isSelected) Color(0xFF3B82F6) else Color.Transparent,
            ) {
                Text(
                    text = slot.name,
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 24.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
fun MoodBowl(
    options: List<MoodOption>,
    onMoodSelected: (MoodOption) -> Unit
) {
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    val haptics = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth()
            .onSizeChanged { containerSize = it }
            .drawBehind {
                // Glass Bowl Bottom Glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.05f), Color.Transparent),
                        center = center,
                        radius = size.minDimension / 2f
                    )
                )
                // Rim highlight (Glass effect)
                drawCircle(
                    color = Color.White.copy(alpha = 0.1f),
                    style = Stroke(width = 1.dp.toPx()),
                    radius = size.minDimension / 2.1f
                )
                // Top reflection
                drawCircle(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.White.copy(alpha = 0.05f), Color.Transparent),
                        start = Offset(center.x, center.y - size.minDimension/2.2f),
                        end = center
                    ),
                    radius = size.minDimension / 2.2f
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (containerSize != IntSize.Zero) {
            options.forEachIndexed { index, option ->
                FloatingEmoji(
                    option = option,
                    containerSize = containerSize,
                    index = index,
                    totalCount = options.size,
                    onLongPress = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onMoodSelected(option)
                    }
                )
            }
        }
    }
}

@Composable
fun FloatingEmoji(
    option: MoodOption,
    containerSize: IntSize,
    index: Int,
    totalCount: Int,
    onLongPress: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "jiggle")
    
    // Smooth complex movement
    val orbitRadius = containerSize.width * 0.26f
    val baseAngle = (2 * Math.PI * index / totalCount).toFloat()
    
    val phaseX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(Random.nextInt(4000, 6000), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phaseX"
    )
    
    val driftX = sin(phaseX + baseAngle) * 25f
    val driftY = cos(phaseX * 1.5f + baseAngle) * 25f

    val scaleTransition = remember { Animatable(1f) }
    val holdProgress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var isHolding by remember { mutableStateOf(false) }

    val xPos = (cos(baseAngle) * orbitRadius + driftX) / 2.5f
    val yPos = (sin(baseAngle) * orbitRadius + driftY) / 2.5f

    Box(
        modifier = Modifier
            .offset(x = xPos.dp, y = yPos.dp)
            .size(100.dp)
            .scale(scaleTransition.value)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isHolding = true
                        val job = scope.launch {
                            launch { scaleTransition.animateTo(1.3f, tween(800)) }
                            holdProgress.animateTo(1f, tween(800, easing = LinearEasing))
                            onLongPress()
                            holdProgress.snapTo(0f)
                            scaleTransition.animateTo(1f, spring(Spring.DampingRatioMediumBouncy))
                        }
                        tryAwaitRelease()
                        isHolding = false
                        job.cancel()
                        scope.launch {
                            holdProgress.animateTo(0f, tween(200))
                            scaleTransition.animateTo(1f, spring(Spring.DampingRatioLowBouncy))
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Glow layer
        Box(
            modifier = Modifier
                .fillMaxSize(0.7f)
                .blur(30.dp)
                .background(option.color.copy(alpha = 0.4f), CircleShape)
        )
        
        // Progress ring
        if (isHolding || holdProgress.value > 0f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = Color.White.copy(alpha = 0.6f),
                    startAngle = -90f,
                    sweepAngle = 360f * holdProgress.value,
                    useCenter = false,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
        
        // Glass Sphere with mood image
        Surface(
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.08f),
            modifier = Modifier
                .fillMaxSize(0.85f)
                .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                .shadow(12.dp, CircleShape, spotColor = option.color)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                val context = LocalContext.current
                val resId = context.resources.getIdentifier(option.imageResName, "drawable", context.packageName)
                if (resId != 0) {
                    Image(
                        painter = painterResource(resId),
                        contentDescription = option.label,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .fillMaxSize(0.88f)
                            .graphicsLayer {
                                rotationZ = driftX * 0.5f
                            }
                    )
                }
            }
        }
    }
}

@Composable
private fun TodaySummarySection(
    todayBySlot: Map<MoodSlot, MoodOption?>,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White.copy(alpha = 0.02f))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(32.dp))
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MoodSlot.entries.forEach { slot ->
            val opt = todayBySlot[slot]
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (opt != null) Color(0xFF3B82F6).copy(alpha = 0.1f) else Color.White.copy(alpha = 0.02f))
                        .border(
                            width = 1.dp,
                            color = if (opt != null) Color(0xFF3B82F6).copy(alpha = 0.3f) else Color.Transparent,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (opt != null) {
                        val context = LocalContext.current
                        val resId = context.resources.getIdentifier(opt.imageResName, "drawable", context.packageName)
                        if (resId != 0) {
                            Image(
                                painter = painterResource(resId),
                                contentDescription = opt.label,
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier.fillMaxSize(0.85f)
                            )
                        }
                    } else {
                        Box(modifier = Modifier.size(6.dp).background(Color.White.copy(alpha = 0.1f), CircleShape))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    slot.name.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (opt != null) Color.White else Color.White.copy(alpha = 0.2f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

private class SimpleVmFactory<T>(
    private val create: () -> T,
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <VM : androidx.lifecycle.ViewModel> create(modelClass: Class<VM>): VM = create() as VM
}
