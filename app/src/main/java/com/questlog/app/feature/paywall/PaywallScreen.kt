@file:OptIn(ExperimentalMaterial3Api::class)

package com.questlog.app.feature.paywall

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.questlog.app.ui.designsystem.QuestlogCoral
import com.questlog.app.ui.designsystem.QuestlogGray
import com.questlog.app.ui.designsystem.QuestlogPrimaryButton
import com.questlog.app.ui.designsystem.QuestlogSpacing
import com.questlog.app.ui.designsystem.QuestlogViolet
import com.questlog.app.ui.designsystem.QuestlogVioletLight
import com.revenuecat.purchases.Offering
import com.revenuecat.purchases.Package

private val proFeatures = listOf(
    "Custom lists and tags",
    "Premium themes",
    "Advanced stats and insights",
    "Multi-year Year in Gaming history",
    "Cloud backup when available",
)

@Composable
fun PaywallScreen(
    onDismiss: () -> Unit,
    viewModel: PaywallViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val offering = uiState.offering
    val activity = androidx.compose.ui.platform.LocalContext.current as Activity

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Questlog Pro") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            when {
                uiState.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = QuestlogViolet)
                }

                uiState.isPro -> ProStatusContent()

                offering != null -> PaywallContent(
                    offering = offering,
                    onPurchase = { pkg -> viewModel.purchase(activity, pkg) },
                    onRestore = viewModel::restorePurchases,
                )

                uiState.errorMessage != null -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(QuestlogSpacing.Xl),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = uiState.errorMessage!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                }

                else -> UnavailableContent(onRestore = viewModel::restorePurchases)
            }
        }
    }
}

@Composable
private fun ProStatusContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(QuestlogSpacing.Xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(QuestlogViolet.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = QuestlogViolet,
                modifier = Modifier.size(40.dp),
            )
        }
        Spacer(modifier = Modifier.height(QuestlogSpacing.L))
        Text(
            text = "You're a Pro!",
            style = MaterialTheme.typography.headlineLarge,
        )
        Text(
            text = "Thanks for supporting Questlog.",
            style = MaterialTheme.typography.bodyMedium,
            color = QuestlogGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = QuestlogSpacing.S),
        )
    }
}

@Composable
private fun PaywallContent(
    offering: Offering,
    onPurchase: (Package) -> Unit,
    onRestore: () -> Unit,
) {
    val packages = offering.availablePackages
    var selectedPackage by remember(offering) { mutableStateOf(packages.firstOrNull()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(QuestlogSpacing.L),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Hero
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(QuestlogViolet, QuestlogCoral)),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp),
            )
        }

        Spacer(modifier = Modifier.height(QuestlogSpacing.L))

        Text(
            text = "Unlock Pro",
            style = MaterialTheme.typography.headlineLarge,
        )
        Text(
            text = "Organize your gaming life deeper.",
            style = MaterialTheme.typography.bodyMedium,
            color = QuestlogGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = QuestlogSpacing.Xs),
        )

        Spacer(modifier = Modifier.height(QuestlogSpacing.Xl))

        // Features
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        ) {
            Column(modifier = Modifier.padding(QuestlogSpacing.L)) {
                proFeatures.forEach { feature ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = QuestlogSpacing.Xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = QuestlogViolet,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = feature,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = QuestlogSpacing.M),
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(QuestlogSpacing.Xl))

        // Packages
        packages.forEach { pkg ->
            PackageCard(
                pkg = pkg,
                selected = pkg == selectedPackage,
                onClick = { selectedPackage = pkg },
            )
            Spacer(modifier = Modifier.height(QuestlogSpacing.S))
        }

        Spacer(modifier = Modifier.height(QuestlogSpacing.L))

        QuestlogPrimaryButton(
            onClick = { selectedPackage?.let(onPurchase) },
            text = "Start 7-day free trial",
        )

        TextButton(onClick = onRestore) {
            Text("Restore purchases")
        }

        Spacer(modifier = Modifier.height(QuestlogSpacing.M))
    }
}

@Composable
private fun PackageCard(
    pkg: Package,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                QuestlogViolet.copy(alpha = 0.08f)
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        border = if (selected) {
            BorderStroke(2.dp, QuestlogViolet)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(QuestlogSpacing.L),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pkg.product.title.ifBlank { pkg.identifier },
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = pkg.product.priceString,
                    style = MaterialTheme.typography.bodyMedium,
                    color = QuestlogGray,
                    modifier = Modifier.padding(top = QuestlogSpacing.Xs),
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = QuestlogViolet,
                )
            }
        }
    }
}

@Composable
private fun UnavailableContent(onRestore: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(QuestlogSpacing.Xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Pro isn't available right now",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Everything else in Questlog keeps working as usual.",
            style = MaterialTheme.typography.bodyMedium,
            color = QuestlogGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = QuestlogSpacing.S),
        )
        TextButton(onClick = onRestore) {
            Text("Restore purchases")
        }
    }
}
