package com.questlog.app.feature.scan

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.questlog.app.R
import com.questlog.app.ui.designsystem.QuestlogPrimaryButton
import com.questlog.app.ui.designsystem.QuestlogSecondaryButton
import com.questlog.app.ui.designsystem.QuestlogSoftBrown
import com.questlog.app.ui.designsystem.QuestlogSpacing
import com.questlog.app.ui.designsystem.QuestlogTerracotta
import java.util.Locale
import java.util.concurrent.Executors

@Composable
fun ScanScreen(
    onBack: () -> Unit,
    onConfirmed: () -> Unit,
    viewModel: ScanViewModel = hiltViewModel(),
) {
    val candidate by viewModel.candidate.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasPermission) {
            val analyzer = remember { viewModel.createAnalyzer() }
            CameraPreview(
                analyzer = analyzer,
                modifier = Modifier.fillMaxSize(),
            )
            ScanOverlay(
                candidate = candidate,
                onConfirm = {
                    viewModel.confirm()
                    onConfirmed()
                },
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            ScanPermissionContent(
                onAllow = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                onOpenSettings = { openAppSettings(context) },
                modifier = Modifier.fillMaxSize(),
            )
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(QuestlogSpacing.S),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.action_back),
                tint = Color.White,
            )
        }
    }
}

@Composable
private fun CameraPreview(
    analyzer: ScanTextAnalyzer,
    modifier: Modifier = Modifier,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            cameraProvider?.unbindAll()
            executor.shutdown()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            PreviewView(viewContext).also { previewView ->
                previewView.scaleType = PreviewView.ScaleType.FILL_CENTER
                val providerFuture = ProcessCameraProvider.getInstance(viewContext)
                providerFuture.addListener({
                    val provider = providerFuture.get()
                    cameraProvider = provider

                    val preview = Preview.Builder().build().apply {
                        setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .apply { setAnalyzer(executor, analyzer) }

                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analysis,
                    )
                }, ContextCompat.getMainExecutor(viewContext))
            }
        },
    )
}

@Composable
private fun ScanOverlay(
    candidate: String?,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        ScanFrameOverlay(modifier = Modifier.fillMaxSize())

        Text(
            text = stringResource(R.string.scan_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 96.dp)
                .padding(horizontal = QuestlogSpacing.Xl),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = QuestlogSpacing.Xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
        ) {
            Text(
                text = if (candidate != null) {
                    stringResource(
                        R.string.scan_found,
                        candidate.uppercase(Locale.US),
                    )
                } else {
                    stringResource(R.string.scan_searching)
                },
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .background(
                        color = Color.Black.copy(alpha = 0.55f),
                        shape = CircleShape,
                    )
                    .padding(horizontal = QuestlogSpacing.M, vertical = QuestlogSpacing.Xs),
            )

            Spacer(modifier = Modifier.height(QuestlogSpacing.L))

            Button(
                onClick = onConfirm,
                enabled = candidate != null,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = QuestlogTerracotta,
                    contentColor = Color.White,
                    disabledContainerColor = QuestlogSoftBrown.copy(alpha = 0.4f),
                    disabledContentColor = Color.White.copy(alpha = 0.6f),
                ),
                modifier = Modifier.size(76.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = stringResource(R.string.scan_confirm),
                    modifier = Modifier.size(36.dp),
                )
            }
        }
    }
}

@Composable
private fun ScanFrameOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val frameWidth = size.width * 0.8f
        val frameHeight = frameWidth * 4f / 3f
        val left = (size.width - frameWidth) / 2f
        val top = (size.height - frameHeight) / 2f
        val frame = RoundRect(
            rect = Rect(left, top, left + frameWidth, top + frameHeight),
            cornerRadius = CornerRadius(FRAME_CORNER_RADIUS.toPx()),
        )
        val cutout = Path().apply {
            addRect(Rect(0f, 0f, size.width, size.height))
            addRoundRect(frame)
            fillType = PathFillType.EvenOdd
        }
        drawPath(path = cutout, color = Color.Black.copy(alpha = 0.4f))
        drawRoundRect(
            color = QuestlogTerracotta,
            cornerRadius = CornerRadius(FRAME_CORNER_RADIUS.toPx()),
            topLeft = Offset(left, top),
            size = Size(frameWidth, frameHeight),
            style = Stroke(width = FRAME_STROKE.toPx()),
        )
    }
}

@Composable
private fun ScanPermissionContent(
    onAllow: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(QuestlogSpacing.Xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.CameraAlt,
            contentDescription = null,
            tint = QuestlogTerracotta,
            modifier = Modifier.size(64.dp),
        )

        Spacer(modifier = Modifier.height(QuestlogSpacing.L))

        Text(
            text = stringResource(R.string.scan_permission_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )

        Text(
            text = stringResource(R.string.scan_permission_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = QuestlogSoftBrown,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = QuestlogSpacing.S),
        )

        Spacer(modifier = Modifier.height(QuestlogSpacing.Xl))

        QuestlogPrimaryButton(
            onClick = onAllow,
            text = stringResource(R.string.scan_permission_grant),
        )

        Spacer(modifier = Modifier.height(QuestlogSpacing.S))

        QuestlogSecondaryButton(
            onClick = onOpenSettings,
            text = stringResource(R.string.scan_permission_settings),
        )
    }
}

private val FRAME_CORNER_RADIUS = 24.dp
private val FRAME_STROKE = 3.dp

private fun openAppSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null),
    ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
    context.startActivity(intent)
}