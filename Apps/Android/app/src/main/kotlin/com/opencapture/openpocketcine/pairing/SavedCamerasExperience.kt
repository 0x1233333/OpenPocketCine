package com.opencapture.openpocketcine.pairing

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opencapture.openpocketcine.AppModel
import com.opencapture.openpocketcine.AppPanel
import com.opencapture.openpocketcine.LiveType
import com.opencapture.openpocketcine.OpcIcon
import com.opencapture.openpocketcine.LiveTypeDesign
import com.opencapture.openpocketcine.core.ConnectionPhase
import com.opencapture.openpocketcine.session.FoundCamera

@Composable
fun SavedCamerasExperience(model: AppModel) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val twoColumn = maxWidth >= 640.dp || maxWidth > maxHeight
        val tight = maxHeight < 300.dp
        val introWidth = maxOf(if (maxWidth >= 640.dp) 288.dp else 240.dp, maxWidth * 0.36f)
        if (twoColumn) {
            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IntroCard(model, hugsContent = false, tight = tight, Modifier.width(introWidth).fillMaxHeight())
                CameraListCard(model, tight, Modifier.weight(1f).fillMaxHeight())
            }
        } else {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                IntroCard(model, hugsContent = true, tight = tight, Modifier.fillMaxWidth())
                CameraListCard(model, tight, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun IntroCard(model: AppModel, hugsContent: Boolean, tight: Boolean, modifier: Modifier) {
    val phase by model.session.phaseFlow.collectAsState()
    val reconnecting by model.session.isReconnecting.collectAsState()
    val busy = phase.isBusy() || reconnecting
    Column(modifier.startupCard().padding(if (tight) 16.dp else 20.dp)) {
        Text(
            "你的相机。",
            color = StartupColors.ink,
            style = LiveType.ui(24f, FontWeight.Bold, LiveTypeDesign.Rounded),
            maxLines = 1,
        )
        Text(
            "点按已保存的相机重新连接。",
            color = StartupColors.muted,
            style = LiveType.ui(13f, design = LiveTypeDesign.Rounded).copy(lineHeight = 16.sp),
            modifier = Modifier.padding(top = if (tight) 6.dp else 10.dp),
        )
        if (hugsContent) Spacer(Modifier.height(16.dp)) else Spacer(Modifier.weight(1f))
        Column(verticalArrangement = Arrangement.spacedBy(if (tight) 8.dp else 10.dp)) {
            StartupFilledButton(
                "配对新相机",
                enabled = !busy,
                onClick = model::pairNewCamera,
                modifier = Modifier.fillMaxWidth(),
                large = true,
            )
            StartupQuietButton(
                "媒体库",
                onClick = { model.homePanel = AppPanel.MEDIA },
                modifier = Modifier.fillMaxWidth(),
            )
            StartupQuietButton(
                "设置",
                onClick = { model.homePanel = AppPanel.SETTINGS },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun CameraListCard(model: AppModel, tight: Boolean, modifier: Modifier) {
    val found by model.session.found.collectAsState()
    val phase by model.session.phaseFlow.collectAsState()
    val reconnecting by model.session.isReconnecting.collectAsState()
    val busy = phase.isBusy() || reconnecting
    val targetId by model.session.connectionTargetId.collectAsState()
    val connectingLabel = if (reconnecting && phase == ConnectionPhase.SCANNING) {
        "Looking for camera…"
    } else {
        StartupConnectionCopy.phaseLabel(phase, null)
    }
    val scroll = rememberScrollState()
    Column(modifier.startupCard().padding(if (tight) 16.dp else 22.dp)) {
        Text(
            "相机列表",
            color = StartupColors.muted,
            style = LiveType.ui(11f, FontWeight.SemiBold, LiveTypeDesign.Rounded).copy(letterSpacing = 1.4.sp),
        )
        if (!tight) {
            Text(
                "点按相机进行连接",
                color = StartupColors.ink,
                style = LiveType.ui(20f, FontWeight.Bold, LiveTypeDesign.Rounded),
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        Column(
            Modifier.weight(1f).padding(top = 16.dp).fadeOverflowBottom(scroll).verticalScroll(scroll),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            model.savedCameras.forEach { camera ->
                SavedCameraRow(
                    camera = camera,
                    nearby = found.firstOrNull { it.id == camera.id },
                    phase = phase,
                    isBusy = busy,
                    connectionLabel = connectingLabel.takeIf { busy && targetId == camera.id },
                    onCancel = model::cancelPairing,
                    onConnect = { model.reconnect(camera) },
                    onRename = { model.rename(camera, it) },
                    onRemove = { model.forget(camera) },
                )
            }
            if (model.savedCameras.isEmpty()) {
                Text(
                    "还没有保存的相机——点「配对新相机」带你完成。",
                    color = StartupColors.muted,
                    style = LiveType.ui(12f, design = LiveTypeDesign.Rounded),
                )
            }
        }
    }
}

@Composable
private fun SavedCameraRow(
    camera: SavedCamera,
    nearby: FoundCamera?,
    phase: ConnectionPhase,
    isBusy: Boolean,
    connectionLabel: String?,
    onCancel: () -> Unit,
    onConnect: () -> Unit,
    onRename: (String?) -> Unit,
    onRemove: () -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    var rename by remember { mutableStateOf(false) }
    var remove by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf(camera.customName.orEmpty()) }
    val online = nearby != null
    val availability = if (online) StartupColors.ready else StartupColors.muted
    val connectLocked =
        phase == ConnectionPhase.JOINING_WIFI ||
            phase == ConnectionPhase.OPENING_DATALINK ||
            phase == ConnectionPhase.LIVE
    Column(
        Modifier.fillMaxWidth()
            .startupTile(borderColor = availability.copy(alpha = 0.28f))
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(
                Modifier.weight(1f).clickable(enabled = !isBusy && !connectLocked, onClick = onConnect),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    camera.displayName,
                    color = StartupColors.ink,
                    style = LiveType.ui(16f, FontWeight.SemiBold, LiveTypeDesign.Rounded),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    camera.modelName + (camera.lastSSID?.let { " · $it" } ?: ""),
                    color = StartupColors.muted,
                    style = LiveType.ui(13f, design = LiveTypeDesign.Rounded),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box {
                OpcIcon(
                    icon = OpcIcon.ELLIPSIS,
                    contentDescription = "相机选项",
                    tint = StartupColors.muted,
                    modifier =
                        Modifier.clip(CircleShape)
                            .clickable(enabled = !isBusy) { menu = true }
                            .size(44.dp)
                            .padding(13.dp),
                )
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(
                        text = { Text("重命名") },
                        leadingIcon = {
                            OpcIcon(OpcIcon.PENCIL, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        onClick = {
                            menu = false
                            renameText = camera.customName.orEmpty()
                            rename = true
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("移除") },
                        leadingIcon = {
                            OpcIcon(OpcIcon.TRASH, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        onClick = {
                            menu = false
                            remove = true
                        },
                    )
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (connectionLabel != null) {
                Box(Modifier.weight(1f)) { StartupConnectionProgress(connectionLabel) }
                StartupQuietButton("取消", onClick = onCancel, modifier = Modifier.height(44.dp))
            } else {
                StartupStatusPill(if (online) "在线" else "离线", availability)
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier.heightIn(min = 44.dp)
                        .clickable(enabled = !isBusy && !connectLocked, onClick = onConnect),
                    contentAlignment = Alignment.Center,
                ) {
                    StartupConnectChrome(
                        text = if (online) "连接" else "重新连接",
                        filled = online,
                        enabled = !isBusy && !connectLocked,
                    )
                }
            }
        }
    }
    if (rename) {
        AlertDialog(
            onDismissRequest = { rename = false },
            title = { Text("重命名相机") },
            text = {
                Column {
                    Text("给这台相机起个好认的名字。")
                    OutlinedTextField(value = renameText, onValueChange = { renameText = it }, label = { Text("名称") })
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRename(renameText)
                        rename = false
                    }
                ) { Text("保存") }
            },
            dismissButton = { TextButton(onClick = { rename = false }) { Text("取消") } },
        )
    }
    if (remove) {
        AlertDialog(
            onDismissRequest = { remove = false },
            title = { Text("移除相机？") },
            text = { Text("这会从手机上移除 ${camera.displayName}，之后可以重新配对。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemove()
                        remove = false
                    }
                ) { Text("移除") }
            },
            dismissButton = { TextButton(onClick = { remove = false }) { Text("取消") } },
        )
    }
}
