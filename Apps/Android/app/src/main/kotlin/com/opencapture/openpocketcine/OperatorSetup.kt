package com.opencapture.openpocketcine

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.SystemClock
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opencapture.monitorui.MonitorLinkHealth
import com.opencapture.openpocketcine.assists.CrushClipCompensation
import com.opencapture.openpocketcine.settings.SettingsFalseColorKey
import com.opencapture.openpocketcine.assists.FalseColorScale
import com.opencapture.openpocketcine.assists.HistogramAssist
import com.opencapture.openpocketcine.assists.LiveAssistState
import com.opencapture.openpocketcine.assists.LiveAssistTool
import com.opencapture.openpocketcine.assists.LiveZebra
import com.opencapture.openpocketcine.assists.ParadeMode
import com.opencapture.openpocketcine.assists.PeakingColor
import com.opencapture.openpocketcine.session.CameraCommands
import com.opencapture.openpocketcine.assists.PeakingSense
import com.opencapture.openpocketcine.assists.ScopeGuides
import com.opencapture.openpocketcine.assists.VectorscopeZoom
import com.opencapture.openpocketcine.assists.WaveformMode
import com.opencapture.openpocketcine.assists.ZebraEditor
import com.opencapture.openpocketcine.assists.ZebraPaint
import com.opencapture.openpocketcine.assists.ZebraUnit
import com.opencapture.openpocketcine.core.ConnectionPhase
import com.opencapture.openpocketcine.diagnostics.DiagnosticCenter
import com.opencapture.openpocketcine.diagnostics.ManualProblemReport
import com.opencapture.openpocketcine.diagnostics.ManualProblemReportDialog
import com.opencapture.openpocketcine.diagnostics.ManualProblemReportDelivery
import com.opencapture.openpocketcine.diagnostics.ReliabilityReporting
import com.opencapture.openpocketcine.feed.FeedUpscaler
import com.opencapture.openpocketcine.feed.LutLookResolver
import com.opencapture.openpocketcine.feed.MonitorTransfer
import com.opencapture.openpocketcine.lut.LUTPicker
import com.opencapture.openpocketcine.settings.DisplayToggleItem
import com.opencapture.openpocketcine.settings.PanelCloseButton
import com.opencapture.openpocketcine.settings.SettingsValueSlider
import com.opencapture.openpocketcine.settings.SettingsActionPill
import com.opencapture.openpocketcine.settings.SettingsColorDot
import com.opencapture.openpocketcine.settings.SettingsColorDots
import com.opencapture.openpocketcine.settings.SettingsCrushClipSegmented
import com.opencapture.openpocketcine.settings.SettingsDashScale
import com.opencapture.openpocketcine.settings.SettingsGroupCard
import com.opencapture.openpocketcine.settings.SettingsInlineRow
import com.opencapture.openpocketcine.settings.SettingsPalette
import com.opencapture.openpocketcine.settings.SettingsPercentSlider
import com.opencapture.openpocketcine.settings.SettingsRowCard
import com.opencapture.openpocketcine.settings.SettingsSegmented
import com.opencapture.openpocketcine.settings.SettingsSwitchInlineRow
import com.opencapture.openpocketcine.settings.SettingsSwitchRow
import com.opencapture.openpocketcine.settings.SettingsValueText
import com.opencapture.openpocketcine.settings.settingsClickable
import java.io.File
import java.util.Locale
import kotlinx.coroutines.delay

object SettingsHelpCopy {
    const val CURRENT_TRANSPORT =
        "Pocket 先用蓝牙配对，再用相机自身的 Wi-Fi 传输 HEVC。此版本不支持 USB-C、热点和 HDMI 采集。"
    const val PHASE = "蓝牙 → Wi-Fi → 数据链路握手当前进行到哪一步。"
    const val CAMERA_WIFI =
        "本次会话已加入该 SSID。密码只保存在本手机的 Android Keystore 中。"
    const val SAVED_CAMERAS =
        "从主页列表配对。「设置」里不能发起新配对——配对入口在「你的相机」。"
    const val EDIT_VIEW = "打开监视器，每个可显示/隐藏的元素都带眼睛开关。"
    const val FRAME_IO =
        "登录后即可在分享面板上传片段。Frame.io 需要联网，上传时手机会暂时离开相机 Wi‑Fi。"
    const val RECORD_CONFIRMATION = "开始/停止录制前先确认，防止误触。"
    const val SHOOTING_MODE =
        "在视频、照片和延时类模式之间切换相机。相机会上报 " +
            "当前模式，因此在机身上做的更改也会同步到这里。"
    const val HAPTICS =
        "开关、设置和云台限位时短促震动确认。连接的手柄在停止时也会震动。"
    const val JOYSTICK_SENSITIVITY =
        "摇杆行程对应多大的云台动作——屏幕摇杆和连接的手柄都适用。轻推慢动，推满最快。4 是默认手感；5 更快达到全速，1 最慢。"
    const val VIRTUAL_JOYSTICK_INVERT_PAN =
        "Reverse left and right on the on-screen stick. Off is the default. A game controller is unchanged."
    const val VIRTUAL_JOYSTICK_INVERT_TILT =
        "Reverse up and down on the on-screen stick. Off is the default. A game controller is unchanged."
    const val VIRTUAL_JOYSTICK_DEADZONE =
        "Ignore small movements near the center. The default is 8%. Increase it to make the center less sensitive."
    const val VIRTUAL_JOYSTICK_RESPONSE =
        "Standard keeps the current feel. Linear responds evenly. Fine makes small movements gentler."
    const val GIMBAL_JOYSTICK =
        "Which analog stick pans and tilts. Left is the default. The other stick does not move the gimbal."
    const val GAMEPAD =
        "A connected game controller. The selected gimbal joystick pans and tilts. Cross/A records. Circle/B recenters. Square/X is rotate-180. Triangle/Y tracks a face. L1/R1 jump zoom out/in. L2/R2 hold-to-zoom (deeper is faster). D-pad up/down ISO, left/right shutter. Unplug rests the stick. On-screen stick wins while you hold it."
    const val KEEP_SCREEN_AWAKE =
        "OpenPocketCine 运行期间阻止自动锁屏。监视器应当常亮。设备过热时系统仍可能调暗屏幕。"
    const val THEME = "炭黑监视器界面配天蓝点缀，为片场低反光环境调校。"
    const val SUPPORT = "连接、实时画面、控制和故障排查。"
    const val REPORT = "在 GitHub 上打开本项目的公开 issue 表单。"
    const val REPORT_PROBLEM =
        "Tell us what happened. Technical details stay off unless you include them. You can attach up to three photos. Sending does not turn on automatic reports."
    const val SHARE_DIAGNOSTICS =
        "生成包含连接事件、警告和崩溃的报告。不含姓名、位置或 Wi-Fi 密码。把复制的文本粘贴到 bug 报告里即可。"
    const val RELIABILITY_REPORTS =
        "Optional: send crash, hang, live-feed reports and session health counts to OpenCapture through Sentry. Off by default. Turn off anytime without losing app features. Uploads wait until you leave camera Wi-Fi. Automatic reports exclude all images. No footage or GPS location. Sentry receives the connection IP; stored event IP and derived geography are removed. See Reporting Privacy below."
    const val RELIABILITY_UNAVAILABLE =
        "This build cannot send automatic reports. You can still share or delete reports stored on this phone."
    const val FEATURE = "在本项目的功能征集讨论中发起想法。"
    const val SOURCE =
        "在 GitHub 上查看 OpenPocketCine 项目。若相机 Wi-Fi 是唯一网络，打开后会离开该网络。"
    const val LINK_HEALTH = "相机链路当前的健康状况——看的是送达质量，不是信号 RSSI。"
    const val CLEAR_CACHE =
        "删除本手机上已下载的片段文件。片段列表保留，之后可随时从相机重新缓存。"
    const val LUT_LOOK = "LUT 只影响手机上的画面，相机里的文件不受影响。"
    const val PROTOCOL =
        "相机控制通过蓝牙和相机 Wi-Fi 上的 DUML 协议实现，不包含也不需要 DJI SDK。"
    const val APP_VERSION = "来自原生工程元数据的当前构建版本。"
    const val LOCAL_CACHE = "从相机下载的原片与回放代理。"
    const val CACHE_FULL_RESOLUTION =
        "打开片段时同时下载相机原文件。关闭则只保留 720p 代理以节省空间。分享需要原文件——未缓存时请连接相机。"
    const val FEED_UPSCALER =
        "实时画面如何被放大以铺满面板。相机送出的像素远少于屏幕像素，所以始终需要某种放大。关=普通取样，快速=固定锐化核，高质量=系统空间放大器。\n\nAI 有本质不同：它是机器学习模型，会「推断」相机根本没有拍到的细节。画面看起来最锐，但它添加的细纹理是编造的——看似合理而非真实——可能暗示镜头并没有记录到的清晰度。判断关键合焦请用「高质量」或「快速」，把 AI 当作观看辅助而非证据。\n\n只显示本机支持的选项。"
    const val FALSE_COLOR_SCALE =
        "相机色彩模式会自动选择 D-Log、D-Log2、Rec.709 或 HLG。" +
            "CineStop 涂视频电平 IRE 条纹（绿 41–48、粉 61–70、红=削波）" +
            "覆盖在亮度灰阶上。EL Zone 从 18% 灰起涂 15 个连续档位：" +
            "+6 以上为白、−6 以下为黑。IRE 在亮度灰阶上涂六个视频电平区间：" +
            "亮度灰阶：紫=截止、蓝=近黑、绿=18% 中灰、粉=高一 " +
            "档、黄=接近削波、红=削波。Limits 只涂阴影和" +
            "高光警告，其余颜色保持不变。"
    const val FALSE_COLOR_REFERENCE =
        "伪色开启时，在实时画面上显示小型颜色对照表。"
    const val PEAKING_SENSITIVITY =
        "灵敏度越高，越能捕捉细微边缘，但细节丰富的画面可能出现噪点。"
    const val PEAKING_COLOR = "选择在常见场景上依然醒目的描边颜色。"
    const val ZEBRA_UNITS =
        "在原生 0-255 编码值和 0-100 监看 IRE 刻度间切换。"
    const val ZEBRA_HIGHLIGHT =
        "高斑马线：在补偿当前 log 曲线后，高光细节接近削波时警告。"
    const val ZEBRA_MIDTONE =
        "中斑马线：给出经曲线补偿的参考带，用于人脸或主体曝光。"
    const val WAVEFORM_BRIGHTNESS =
        "强光下波形难读时，提高轨迹亮度。"
    const val PARADE_BRIGHTNESS = "通道分离难辨认时，提高轨迹亮度。"
    const val VECTORSCOPE_ZOOM =
        "只放大色度轨迹，刻度线保持不变。矢量示波器读取的是监视画面（你的活动 LUT 或内置显示色调映射），这里的色度才有意义。"
    const val VECTORSCOPE_BRIGHTNESS =
        "色度图难读时，提高轨迹亮度。"
    const val TRAFFIC_LIGHTS_COMPENSATION =
        "通道指示灯点亮前的暗部截止/高光削波容差档数。与直方图红绿灯共用。"
}

object OpenPocketCineLinks {
    const val SOURCE = "https://github.com/erik-sutton95/OpenPocketCine"
    const val SUPPORT = "https://github.com/erik-sutton95/OpenPocketCine/discussions/categories/q-a"
    const val REPORT_PROBLEM =
        "https://github.com/erik-sutton95/OpenPocketCine/issues/new?template=bug_report.yml"
    const val FEATURE_REQUEST =
        "https://github.com/erik-sutton95/OpenPocketCine/discussions/new?category=ideas"
    const val PRIVACY = "https://openpocketcine.app/privacy/"
    const val TERMS = "https://openpocketcine.app/terms/"
}

internal object OperatorLinkHealth {
    const val TARGET_FPS = 25.0

    fun bars(
        isLive: Boolean,
        videoPackets: Int,
        hasVideoFormat: Boolean,
        measuredFps: Double = -1.0,
    ): Int {
        if (!isLive) return 0
        if (measuredFps > 0.0) {
            val score = ((measuredFps / TARGET_FPS) * 100.0).coerceIn(0.0, 100.0)
            val rounded = kotlin.math.round(score).toInt()
            if (rounded <= 0) return 1
            return ((rounded + 24) / 25).coerceIn(1, 4)
        }
        return when {
            hasVideoFormat && videoPackets > 0 -> 4
            videoPackets >= 400 -> 4
            videoPackets >= 120 -> 3
            videoPackets >= 1 -> 2
            else -> 1
        }
    }

    fun score(bars: Int): Int = MonitorLinkHealth.score(bars)

    fun caption(isLive: Boolean, bars: Int): String {
        if (!isLive) return "无实时画面通道。"
        if (bars <= 0) return "Waiting for the link."
        return when (MonitorLinkHealth.band(score(bars))) {
            MonitorLinkHealth.Band.STABLE -> "Link is clean. · Stable"
            MonitorLinkHealth.Band.WATCH -> "Some loss on the link. · Watch"
            MonitorLinkHealth.Band.POOR -> "Link is weak. · Poor"
        }
    }

    fun compactFps(incoming: String): String {
        val compact = if (incoming.endsWith(".00")) incoming.dropLast(3) else incoming
        return compact.ifEmpty { "—" }
    }

    fun formatMeasuredFps(fps: Double): String {
        if (fps <= 0.0) return ""
        val rounded = kotlin.math.round(fps)
        return if (kotlin.math.abs(fps - rounded) < 0.05) {
            rounded.toInt().toString()
        } else {
            String.format(Locale.US, "%.2f", fps)
        }
    }

    fun fpsChipLabel(
        isLive: Boolean,
        recovering: Boolean,
        measuredFps: Double,
        phase: ConnectionPhase,
    ): String {
        if (phase == ConnectionPhase.FAILED) return "失败"
        if (recovering) return "重连中"
        if (measuredFps > 0.0) return compactFps(formatMeasuredFps(measuredFps))
        return if (!isLive && phase == ConnectionPhase.IDLE) "—" else "连接"
    }

    fun liveTileDetail(
        isLive: Boolean,
        cameraName: String,
        fpsLabel: String,
        phaseLabel: String,
    ): String = if (isLive) "$cameraName · 蓝牙 + Wi-Fi · $fpsLabel FPS" else phaseLabel
}

internal object OperatorMediaCache {
    fun candidates(context: Context): List<File> =
        listOf(
            File(context.filesDir, "OpenPocketCine/media"),
            File(context.filesDir, "media-cache"),
            File(context.cacheDir, "media-cache"),
        )

    fun existingDir(context: Context): File? = candidates(context).firstOrNull { it.isDirectory }

    fun byteCount(context: Context): Long {
        val dir = existingDir(context) ?: return 0L
        return dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    fun clear(context: Context) {
        val mediaRoot = File(context.filesDir, "OpenPocketCine/media")
        if (mediaRoot.isDirectory) {
            mediaRoot.listFiles()?.forEach { cameraDir ->
                if (!cameraDir.isDirectory) return@forEach
                cameraDir.listFiles()?.forEach { child ->
                    if (child.name != "index.json") child.deleteRecursively()
                }
            }
            return
        }
        existingDir(context)?.listFiles()?.forEach { it.deleteRecursively() }
    }
}

internal fun formatCacheSize(bytes: Long): String {
    if (bytes <= 0L) return "空"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var unit = 0
    while (value >= 1024.0 && unit < units.lastIndex) {
        value /= 1024.0
        unit++
    }
    return if (unit == 0) "$bytes B" else String.format(Locale.US, "%.1f %s", value, units[unit])
}

internal fun formatAppVersion(versionName: String, versionCode: Long): String = "$versionName ($versionCode)"

internal fun lutLookLabel(
    selection: String,
    enabled: Boolean = true,
    colorMode: Int = -1,
    family: String = "pocket",
    cameraName: String? = null,
): String {
    val source =
        LutLookResolver.resolve(
            selection = selection,
            lutOn = true,
            colorMode = colorMode,
            family = family,
            cameraName = cameraName,
        )
    return LutLookResolver.statusLabel(enabled, selection, source)
}

internal fun toggledCleanPins(current: Set<String>, toolKey: String): Set<String> {
    val next = current.toMutableSet()
    if (!next.add(toolKey)) next.remove(toolKey)
    return OperatorPrefs.resolvedCleanPins(next)
}

internal fun lutPickerAvailable(): Boolean = true

internal enum class CleanPinTool(val key: String, val title: String) {
    LUT("LUT", "LUT"),
    PEAKING("PEAK", "峰值对焦"),
    FALSE_COLOR("FALSE", "伪色"),
    ZEBRA("ZEBRA", "斑马纹"),
    WAVEFORM("WAVE", "波形图"),
    PARADE("PARADE", "分量图"),
    HISTOGRAM("HISTO", "直方图"),
    VECTORSCOPE("VECTOR", "矢量示波器"),
    TRAFFIC_LIGHTS("LIGHTS", "红绿灯提示"),
    ND("ND", "ND 建议"),
    GUIDES("GUIDES", "参考线"),
    GRID("GRID", "网格"),
    CROSSHAIR("CROSS", "十字线"),
    MIRROR("MIRROR", "镜像"),
    AUDIO("AUDIO", "音频电平"),
}

internal enum class AssistCard(val title: String) {
    FALSE_COLOR("伪色"),
    WAVEFORM("波形图"),
    HISTOGRAM("直方图"),
    PEAKING("峰值对焦"),
    ZEBRA("斑马纹"),
    PARADE("分量图"),
    VECTORSCOPE("矢量示波器"),
    TRAFFIC_LIGHTS("红绿灯提示"),
}

internal fun connectionPhaseLabel(phase: ConnectionPhase, failure: String?): String =
    when (phase) {
        ConnectionPhase.IDLE -> "空闲"
        ConnectionPhase.SCANNING -> "正在搜索相机…"
        ConnectionPhase.CONNECTING_GATT -> "正在连接（蓝牙）…"
        ConnectionPhase.PAIRING -> "正在配对…"
        ConnectionPhase.AWAITING_APPROVAL -> "在相机屏幕上确认"
        ConnectionPhase.READING_WIFI_CREDS -> "正在读取 Wi-Fi 信息…"
        ConnectionPhase.JOINING_WIFI -> "正在加入相机 Wi-Fi…"
        ConnectionPhase.OPENING_DATALINK -> "正在打开数据链路…"
        ConnectionPhase.LIVE -> "已连接"
        ConnectionPhase.FAILED ->
            if (failure.isNullOrBlank()) "失败" else "失败：$failure"
    }

internal fun resetDispChrome(model: AppModel, mode: PocketDispMode) {
    val defaults =
        if (mode == PocketDispMode.LIVE) PocketDispChrome.liveDefaults else PocketDispChrome.cleanDefaults
    PocketDispSection.entries.forEach { section ->
        if (model.chrome(mode).isVisible(section) != defaults.isVisible(section)) {
            model.toggleChrome(section, mode)
        }
    }
}

@Composable
fun OperatorSetupScreen(model: AppModel, onClose: () -> Unit) {
    BackHandler(onBack = onClose)
    val phase by model.session.phaseFlow.collectAsState()
    val failure by model.session.failure.collectAsState()
    val recovery by model.session.recoveryState.collectAsState()
    val status by model.session.status.collectAsState()
    var tick by remember { mutableIntStateOf(0) }
    var lastFrames by remember { mutableIntStateOf(0) }
    var lastTickAt by remember { mutableStateOf(0L) }
    var measuredFps by remember { mutableStateOf(0.0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            tick += 1
            val now = SystemClock.elapsedRealtime()
            val frames = model.session.decoder.framesEnqueued.get()
            if (lastTickAt != 0L) {
                val dt = (now - lastTickAt) / 1000.0
                val presentedAge = model.session.decoder.lastPresentedAt?.let { now - it }
                measuredFps =
                    if (dt > 0 && presentedAge != null && presentedAge < 1_500) {
                        ((frames - lastFrames) / dt).coerceAtLeast(0.0)
                    } else {
                        0.0
                    }
            }
            lastFrames = frames
            lastTickAt = now
        }
    }
    tick
    val isLive = phase == ConnectionPhase.LIVE
    val bars =
        OperatorLinkHealth.bars(
            isLive,
            model.session.videoPackets,
            model.session.hasVideoFormat,
            measuredFps,
        )
    val fpsLabel =
        OperatorLinkHealth.fpsChipLabel(
            isLive = isLive,
            recovering = recovery.isRecovering,
            measuredFps = measuredFps,
            phase = phase,
        )
    val phaseLabel = connectionPhaseLabel(phase, failure)
    val view = LocalView.current
    val hapticsEnabled = model.hapticsEnabled
    var legalKind by remember { mutableStateOf<LegalKind?>(null) }
    var expandedDisp by remember { mutableStateOf<PocketDispMode?>(null) }
    var confirmClearCache by remember { mutableStateOf(false) }
    var showLutPicker by remember { mutableStateOf(false) }

    LaunchedEffect(model.chromeEditorReturnMode) {
        val returning = model.chromeEditorReturnMode ?: return@LaunchedEffect
        expandedDisp = returning
        model.chromeEditorReturnMode = null
    }

    CompositionLocalProvider(LocalMonitorGlass provides null) {
    Box(
        Modifier
            .fillMaxSize()
            .background(LiveDesign.background)
            .pointerInput(Unit) { detectTapGestures {} }
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        com.opencapture.openpocketcine.monitor.MonitorPageScaffold(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            heading = { com.opencapture.openpocketcine.monitor.MonitorPageHeading("Operator Setup", "OPENPOCKETCINE") },
            navigation = { portrait ->
                Column(
                    if (portrait) Modifier.fillMaxWidth() else Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(if (portrait) 9.dp else 8.dp),
                ) {
                    if (portrait) {
                        SettingsTabStrip(model, hapticsEnabled, view)
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            SettingsSessionStatus(isLive, phaseLabel, Modifier.weight(1f))
                            if (isLive) {
                                SettingsActionPill(
                                    "Disconnect",
                                    OpcIcon.LINK_2_OFF,
                                    LiveDesign.rec,
                                    LiveDesign.rec.copy(alpha = .12f),
                                    onClick = model::disconnect,
                                )
                            }
                        }
                    } else {
                        Column(
                            Modifier.weight(1f).verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            OperatorSettingsTab.entries.forEach { tab ->
                                SettingsTabButton(tab, model, hapticsEnabled, view, Modifier.fillMaxWidth())
                            }
                        }
                        SettingsSessionStatus(isLive, phaseLabel, Modifier.fillMaxWidth())
                        if (isLive) {
                            SettingsActionPill(
                                "Disconnect",
                                OpcIcon.LINK_2_OFF,
                                LiveDesign.rec,
                                LiveDesign.rec.copy(alpha = .12f),
                                onClick = model::disconnect,
                            )
                        }
                    }
                }
            },
        ) {
            SettingsContentPane(model = model, isLive = isLive, phaseLabel = phaseLabel,
                bars = bars, statusColorMode = status.monitorColorMode, expandedDisp = expandedDisp,
                onExpandDisp = { expandedDisp = it }, onLegal = { legalKind = it },
                onClearCache = { confirmClearCache = true }, onOpenLut = { showLutPicker = true })
        }
        legalKind?.let { kind ->
            LegalDocumentScreen(kind = kind, onClose = { legalKind = null })
        }
        if (showLutPicker) {
            LutPickerHost(model = model, onClose = { showLutPicker = false })
        }
        if (confirmClearCache) {
            val context = LocalContext.current
            AlertDialog(
                onDismissRequest = { confirmClearCache = false },
                title = { Text("清除缓存？", color = LiveDesign.text) },
                text = {
                    Text(
                        "删除本手机上已下载的片段文件。片段列表会保留。",
                        color = LiveDesign.muted,
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            OperatorMediaCache.clear(context)
                            confirmClearCache = false
                        },
                    ) {
                        Text("清除", color = LiveDesign.rec)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { confirmClearCache = false }) {
                        Text("取消", color = LiveDesign.muted)
                    }
                },
                containerColor = LiveDesign.surface,
            )
        }
    }
    }
}

@Composable
fun AppSettingsScreen(model: AppModel, onClose: () -> Unit = { model.homePanel = null }) {
    OperatorSetupScreen(model, onClose)
}

private fun liveCameraName(model: AppModel): String =
    model.session.connectedCamera?.name
        ?: model.savedCameras.firstOrNull()?.displayName
        ?: "Pocket"

@Composable
private fun LutPickerHost(model: AppModel, onClose: () -> Unit) {
    LUTPicker(model = model, onClose = onClose)
}

@Composable
private fun SettingsTopBar(
    stacked: Boolean,
    isLive: Boolean,
    phaseLabel: String,
    bars: Int,
    cameraName: String,
    fpsLabel: String,
    hasVideoFormat: Boolean,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    "OPENPOCKETCINE",
                    style = LiveType.ui(9.5f, FontWeight.Bold).copy(letterSpacing = 0.8.sp),
                    color = LiveDesign.accent,
                )
                Text(
                    "Settings",
                    style = LiveType.title(24f, FontWeight.SemiBold),
                    color = LiveDesign.text,
                    maxLines = 1,
                )
            }
            if (!stacked) {
                SessionControls(isLive, phaseLabel, bars, cameraName, fpsLabel, hasVideoFormat, onDisconnect)
            }
        }
        if (stacked) {
            SessionControls(isLive, phaseLabel, bars, cameraName, fpsLabel, hasVideoFormat, onDisconnect)
        }
    }
}

@Composable
private fun SessionControls(
    isLive: Boolean,
    phaseLabel: String,
    bars: Int,
    cameraName: String,
    fpsLabel: String,
    hasVideoFormat: Boolean,
    onDisconnect: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        if (isLive) {
            SettingsActionPill(
                title = "断开连接",
                icon = OpcIcon.UNPLUG,
                tint = LiveDesign.rec,
                background = LiveDesign.rec.copy(alpha = 0.16f),
                onClick = onDisconnect,
            )
        }
        SettingsLiveTile(
            isLive = isLive,
            phaseLabel = phaseLabel,
            bars = bars,
            cameraName = cameraName,
            fpsLabel = fpsLabel,
            hasVideoFormat = hasVideoFormat,
        )
    }
}

@Composable
private fun SettingsTabRail(model: AppModel, hapticsEnabled: Boolean, view: View) {
    Column(
        Modifier
            .width(146.dp)
            .fillMaxHeight()
            .panelGlass(ChromeShape)
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        OperatorSettingsTab.entries.forEach { tab ->
            SettingsTabButton(tab, model, hapticsEnabled, view, Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun SettingsTabStrip(
    model: AppModel,
    hapticsEnabled: Boolean,
    view: View,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()
    Row(
        modifier
            .fillMaxWidth()
            .height(44.dp)
            .horizontalScroll(scroll)
            .testTag("monitor.settings.tabs"),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OperatorSettingsTab.entries.forEach { tab ->
            SettingsTabButton(tab, model, hapticsEnabled, view, Modifier.wrapContentWidth())
        }
    }
}

@Composable
private fun SettingsTabButton(
    tab: OperatorSettingsTab,
    model: AppModel,
    hapticsEnabled: Boolean,
    view: View,
    modifier: Modifier = Modifier,
) {
    val selected = model.operatorSettingsTab == tab
    val bringIntoView = remember { BringIntoViewRequester() }
    LaunchedEffect(selected) { if (selected) bringIntoView.bringIntoView() }
    Row(
        modifier
            .height(44.dp)
            .background(if (selected) Color.White.copy(alpha = .08f) else Color.Transparent, RoundedCornerShape(9.dp))
            .bringIntoViewRequester(bringIntoView)
            .testTag("monitor.settings.tab.${tab.title}")
            .semantics {
                contentDescription = tab.title
                this.selected = selected
            }
            .settingsClickable(role = Role.Tab) {
                if (tab != model.operatorSettingsTab) operatorHaptic(view, hapticsEnabled)
                model.operatorSettingsTab = tab
            }
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Box(
            Modifier
                .width(4.dp)
                .height(24.dp)
                .background(
                    if (selected) LiveDesign.accent else LiveDesign.accent.copy(alpha = 0f),
                    RoundedCornerShape(3.dp),
                ),
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                tab.title,
                style = LiveType.ui(12.5f, FontWeight.SemiBold),
                color = if (selected) LiveDesign.text else LiveDesign.muted,
                maxLines = 1,
            )
            Text(
                tab.rail,
                style = LiveType.ui(10f),
                color = LiveDesign.faint,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun SettingsContentPane(
    model: AppModel,
    isLive: Boolean,
    phaseLabel: String,
    bars: Int,
    statusColorMode: Int,
    expandedDisp: PocketDispMode?,
    onExpandDisp: (PocketDispMode?) -> Unit,
    onLegal: (LegalKind) -> Unit,
    onClearCache: () -> Unit,
    onOpenLut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tab = model.operatorSettingsTab
    Column(
        modifier.fillMaxSize(),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(tab.title, style = LiveType.title(17f, FontWeight.SemiBold), color = LiveDesign.text)
                Text(tab.subtitle, style = LiveType.ui(10.5f), color = LiveDesign.muted, maxLines = 2)
            }
            Text(
                tab.pill.uppercase(),
                style = LiveType.mono(10f, FontWeight.Bold).copy(letterSpacing = 0.6.sp, color = LiveDesign.accent),
                modifier =
                    Modifier
                        .border(1.dp, LiveDesign.accentDim, CircleShape)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
            )
        }
        Spacer(Modifier.height(10.dp))
        key(tab) {
            val scroll = rememberScrollState()
            Box(Modifier.weight(1f).fillMaxWidth()) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(scroll)
                        .padding(bottom = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    when (tab) {
                        OperatorSettingsTab.LINK ->
                            LinkRows(model, isLive, phaseLabel, bars)
                        OperatorSettingsTab.SHARING -> SharingRows()
                        OperatorSettingsTab.ASSIST -> AssistRows(model, statusColorMode, onOpenLut)
                        OperatorSettingsTab.CONTROLS -> ControlsRows(model)
                        OperatorSettingsTab.DISPLAY ->
                            DisplayRows(model, isLive, expandedDisp, onExpandDisp)
                        OperatorSettingsTab.STORAGE -> StorageRows(model, onClearCache)
                        OperatorSettingsTab.SYSTEM -> SystemRows(model, onLegal)
                    }
                }
                if (scroll.canScrollForward) {
                    ScrollMoreCue(Modifier.align(Alignment.BottomCenter))
                }
            }
        }
    }
}

@Composable
private fun LinkRows(model: AppModel, isLive: Boolean, phaseLabel: String, bars: Int) {
    SettingsDashScale(
        title = "链路健康",
        caption = OperatorLinkHealth.caption(isLive, bars),
        score = OperatorLinkHealth.score(bars),
    )
    SettingsRowCard(title = "连接") {
        SettingsInlineRow("当前传输方式", SettingsHelpCopy.CURRENT_TRANSPORT, showTopDivider = false) {
            SettingsValueText(if (isLive) "蓝牙 + Wi-Fi 已连接" else "未连接")
        }
        SettingsInlineRow("阶段", SettingsHelpCopy.PHASE) {
            SettingsValueText(phaseLabel)
        }
        val ssid = model.session.joinedSSID
        if (!ssid.isNullOrEmpty()) {
            SettingsInlineRow("相机 Wi-Fi", SettingsHelpCopy.CAMERA_WIFI) {
                SettingsValueText(ssid)
            }
        }
    }
    if (FeedUpscaler.supported.size > 1) {
        val context = LocalContext.current
        var upscaler by remember { mutableStateOf(OperatorPrefs.feedUpscaler(context)) }
        SettingsRowCard(title = "处理中") {
            SettingsInlineRow("画面放大", SettingsHelpCopy.FEED_UPSCALER, showTopDivider = false) {
                SettingsSegmented(
                    options = FeedUpscaler.supported.map { it.label },
                    selected = upscaler.label,
                    compact = true,
                    fillWidth = false,
                ) { label ->
                    val next = FeedUpscaler.fromStored(label)
                    upscaler = next
                    OperatorPrefs.setFeedUpscaler(context, next)
                }
            }
        }
    }
    SettingsRowCard(title = "你的相机") {
        if (model.savedCameras.isEmpty()) {
            SettingsInlineRow("已保存", SettingsHelpCopy.SAVED_CAMERAS, showTopDivider = false) {
                SettingsValueText("无")
            }
        } else {
            model.savedCameras.forEachIndexed { index, camera ->
                val help = camera.modelName + (camera.lastSSID?.let { " · $it" } ?: "")
                SettingsInlineRow(camera.displayName, help, showTopDivider = index > 0) {
                    SettingsValueText(camera.lastSSID ?: "已保存")
                }
            }
        }
    }
}

@Composable
private fun SharingRows() {
    SettingsRowCard {
        Text(
            "敬请期待…",
            style = LiveType.ui(15f, FontWeight.Medium),
            color = LiveDesign.muted,
            modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp, horizontal = 2.dp),
        )
    }
}

@Composable
private fun AssistRows(model: AppModel, statusColorMode: Int, onOpenLut: () -> Unit) {
    val assist = model.assist
    val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

    if (isPortrait) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FalseColorAssistCard(assist, statusColorMode)
            ZebraAssistCard(assist, statusColorMode)
            WaveformAssistCard(assist)
            ParadeAssistCard(assist)
            HistogramAssistCard(assist)
            VectorscopeAssistCard(assist)
            PeakingAssistCard(assist)
            TrafficLightsAssistCard(assist)
        }
    } else {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FalseColorAssistCard(assist, statusColorMode)
                WaveformAssistCard(assist)
                HistogramAssistCard(assist)
                PeakingAssistCard(assist)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ZebraAssistCard(assist, statusColorMode)
                ParadeAssistCard(assist)
                VectorscopeAssistCard(assist)
                TrafficLightsAssistCard(assist)
            }
        }
    }

    SettingsRowCard(title = "LUT") {
        SettingsInlineRow("风格", SettingsHelpCopy.LUT_LOOK, showTopDivider = false) {
            SettingsValueText(
                lutLookLabel(
                    selection = model.lutSelection,
                    enabled = assist.lutOn,
                    colorMode = statusColorMode,
                    family = model.session.connectedCamera?.model?.family ?: "pocket",
                    cameraName = model.session.connectedCamera?.name,
                ),
            )
        }
        SettingsInlineRow("选择 LUT") {
            SettingsActionPill(title = "打开", onClick = onOpenLut)
        }
    }
}

@Composable
private fun FalseColorAssistCard(assist: LiveAssistState, colorMode: Int) {
    SettingsRowCard(
        title = "伪色",
        onReset = {
            assist.setFalseColor(scale = FalseColorScale.STOPS, reference = true)
        },
    ) {
        SettingsInlineRow(
            "标尺",
            SettingsHelpCopy.FALSE_COLOR_SCALE,
            showTopDivider = false,
            stacked = true,
        ) {
            SettingsSegmented(
                options = listOf("CineStop 档位", "EL Zone 曝光", "IRE 电平", "限值"),
                selected = assist.falseColorScale.menuLabel,
            ) { label ->
                assist.setFalseColor(scale = FalseColorScale.fromMenuLabel(label))
            }
        }
        SettingsInlineRow(title = "Reference key", stacked = true) {
            SettingsFalseColorKey(assist.falseColorScale, colorMode)
        }
        SettingsSwitchInlineRow(
            title = "基准显示器",
            isOn = assist.falseColorReference,
            help = SettingsHelpCopy.FALSE_COLOR_REFERENCE,
            stacked = true,
        ) {
            assist.setFalseColor(reference = !assist.falseColorReference)
        }
    }
}

@Composable
private fun PeakingAssistCard(assist: LiveAssistState) {
    SettingsRowCard(
        title = "峰值对焦",
        onReset = { assist.setPeaking(color = PeakingColor.RED, sense = PeakingSense.MED) },
    ) {
        SettingsInlineRow(
            "灵敏度",
            SettingsHelpCopy.PEAKING_SENSITIVITY,
            showTopDivider = false,
            stacked = true,
        ) {
            SettingsSegmented(
                options = PeakingSense.entries.map { it.label },
                selected = assist.peakingSensitivity.label,
            ) { label ->
                assist.setPeaking(sense = PeakingSense.fromPersisted(label))
            }
        }
        SettingsInlineRow("颜色", SettingsHelpCopy.PEAKING_COLOR, stacked = true) {
            SettingsColorDots(
                dots = SettingsPalette.peaking,
                selectedName = assist.peakingColor.label,
            ) { name ->
                assist.setPeaking(color = PeakingColor.fromPersisted(name))
            }
        }
    }
}

@Composable
private fun ZebraAssistCard(assist: LiveAssistState, colorMode: Int) {
    SettingsRowCard(
        title = "斑马纹",
        onReset = {
            assist.updateZebraUnit(ZebraUnit.IRE)
            assist.setZebraHighlight(enabled = true, ire = LiveZebra.HIGHLIGHT_IRE, color = ZebraPaint.WHITE)
            assist.setZebraMidtone(enabled = true, ire = LiveZebra.MIDTONE_IRE, color = ZebraPaint.AMBER)
        },
    ) {
        SettingsInlineRow(
            "单位",
            SettingsHelpCopy.ZEBRA_UNITS,
            showTopDivider = false,
            stacked = true,
        ) {
            SettingsSegmented(
                options = listOf("0-255", "IRE"),
                selected = assist.zebraUnit.editorLabel,
            ) { label ->
                assist.updateZebraUnit(ZebraUnit.fromEditorLabel(label))
            }
        }
        val transfer = MonitorTransfer.fromColorMode(colorMode)
        val maximum = ZebraEditor.editorMaximum(assist.zebraUnit)
        ZebraZoneRow(
            title = "高光",
            help = SettingsHelpCopy.ZEBRA_HIGHLIGHT,
            enabled = assist.zebraHighlight,
            value = ZebraEditor.displayValue(assist.zebraHighlightIRE, assist.zebraUnit, transfer),
            maximum = maximum,
            selectedColor = assist.zebraHighlightColor.label,
            palette = SettingsPalette.highlight,
            onEnabled = { assist.setZebraHighlight(enabled = !assist.zebraHighlight) },
            onValue = {
                assist.setZebraHighlight(ire = ZebraEditor.ireFromDisplay(it, assist.zebraUnit, transfer))
            },
            onColor = { assist.setZebraHighlight(color = ZebraPaint.fromPersisted(it)) },
        )
        ZebraZoneRow(
            title = "中间调",
            help = SettingsHelpCopy.ZEBRA_MIDTONE,
            enabled = assist.zebraMidtone,
            value = ZebraEditor.displayValue(assist.zebraMidtoneIRE, assist.zebraUnit, transfer),
            maximum = maximum,
            selectedColor = assist.zebraMidtoneColor.label,
            palette = SettingsPalette.midtone,
            onEnabled = { assist.setZebraMidtone(enabled = !assist.zebraMidtone) },
            onValue = {
                assist.setZebraMidtone(ire = ZebraEditor.ireFromDisplay(it, assist.zebraUnit, transfer))
            },
            onColor = { assist.setZebraMidtone(color = ZebraPaint.fromPersisted(it)) },
        )
    }
}

@Composable
private fun ZebraZoneRow(
    title: String,
    help: String,
    enabled: Boolean,
    value: Int,
    maximum: Int,
    selectedColor: String,
    palette: List<SettingsColorDot>,
    onEnabled: () -> Unit,
    onValue: (Int) -> Unit,
    onColor: (String) -> Unit,
) {
    SettingsSwitchInlineRow(title = title, help = help, isOn = enabled, onToggle = onEnabled)
    Column(
        Modifier
            .padding(start = 14.dp)
            .alpha(if (enabled) 1f else 0.4f),
    ) {
        SettingsInlineRow(title = "Threshold") {
            SettingsValueSlider(
                value = value.coerceIn(0, maximum),
                range = 0..maximum,
                label = "$value",
                labelWidth = 34,
                onChange = onValue,
            )
        }
        SettingsInlineRow(title = "Colour") {
            SettingsColorDots(dots = palette, selectedName = selectedColor, onSelect = onColor)
        }
    }
}

@Composable
private fun WaveformAssistCard(assist: LiveAssistState) {
    SettingsRowCard(
        title = "波形图",
        onReset = { assist.setWaveform(mode = WaveformMode.RGB, brightness = 100, guides = ScopeGuides()) },
    ) {
        SettingsInlineRow("模式", showTopDivider = false, stacked = true) {
            SettingsSegmented(
                options = WaveformMode.entries.map { it.label },
                selected = assist.waveMode.label,
            ) { label ->
                assist.setWaveform(mode = WaveformMode.fromPersisted(label))
            }
        }
        SettingsInlineRow("亮度", SettingsHelpCopy.WAVEFORM_BRIGHTNESS, stacked = true) {
            SettingsPercentSlider(value = assist.waveBrightness, range = 0..200) {
                assist.setWaveform(brightness = it)
            }
        }
        ScopeGuideRows(assist.waveGuides) { assist.setWaveform(guides = it) }
    }
}

@Composable
private fun ParadeAssistCard(assist: LiveAssistState) {
    SettingsRowCard(
        title = "分量图",
        onReset = { assist.setParade(mode = ParadeMode.RGB, brightness = 100, guides = ScopeGuides()) },
    ) {
        SettingsInlineRow("模式", showTopDivider = false, stacked = true) {
            SettingsSegmented(
                options = ParadeMode.entries.map { it.label },
                selected = assist.paradeMode.label,
            ) { label ->
                assist.setParade(mode = ParadeMode.fromPersisted(label))
            }
        }
        SettingsInlineRow("亮度", SettingsHelpCopy.PARADE_BRIGHTNESS, stacked = true) {
            SettingsPercentSlider(value = assist.paradeBrightness, range = 0..200) {
                assist.setParade(brightness = it)
            }
        }
        ScopeGuideRows(assist.paradeGuides) { assist.setParade(guides = it) }
    }
}

@Composable
private fun HistogramAssistCard(assist: LiveAssistState) {
    SettingsRowCard(
        title = "直方图",
        onReset = {
            assist.setHistogram(traffic = true, compensation = CrushClipCompensation.ZERO)
        },
    ) {
        SettingsSwitchRow(
            title = HistogramAssist.TRAFFIC_LIGHTS_TITLE,
            isOn = assist.histoTrafficLights,
            help = HistogramAssist.TRAFFIC_LIGHTS_HELP,
            showTopDivider = false,
            stacked = true,
        ) {
            assist.setHistogram(traffic = !assist.histoTrafficLights)
        }
        SettingsInlineRow(
            title = HistogramAssist.COMPENSATION_TITLE,
            help = HistogramAssist.COMPENSATION_HELP,
            stacked = true,
        ) {
            CrushClipControl(assist.crushClipCompensation) { assist.setHistogram(compensation = it) }
        }
    }
}

@Composable
private fun VectorscopeAssistCard(assist: LiveAssistState) {
    SettingsRowCard(
        title = "矢量示波器",
        onReset = { assist.setVectorscope(zoom = VectorscopeZoom.X1, brightness = 100) },
    ) {
        SettingsInlineRow(
            "轨迹放大",
            SettingsHelpCopy.VECTORSCOPE_ZOOM,
            showTopDivider = false,
            stacked = true,
        ) {
            SettingsSegmented(
                options = VectorscopeZoom.entries.map { it.label },
                selected = assist.vectorZoom.label,
            ) { label ->
                assist.setVectorscope(zoom = VectorscopeZoom.fromPersisted(label))
            }
        }
        SettingsInlineRow("亮度", SettingsHelpCopy.VECTORSCOPE_BRIGHTNESS, stacked = true) {
            SettingsPercentSlider(value = assist.vectorBrightness, range = 0..200) {
                assist.setVectorscope(brightness = it)
            }
        }
    }
}

@Composable
private fun TrafficLightsAssistCard(assist: LiveAssistState) {
    SettingsRowCard(
        title = "红绿灯提示",
        onReset = { assist.setCompensation(CrushClipCompensation.ZERO) },
    ) {
        SettingsInlineRow(
            title = HistogramAssist.COMPENSATION_TITLE,
            help = SettingsHelpCopy.TRAFFIC_LIGHTS_COMPENSATION,
            showTopDivider = false,
            stacked = true,
        ) {
            CrushClipControl(assist.crushClipCompensation) { assist.setCompensation(it) }
        }
    }
}

@Composable
private fun CrushClipControl(selected: CrushClipCompensation, onSelect: (CrushClipCompensation) -> Unit) {
    SettingsCrushClipSegmented(
        options = CrushClipCompensation.entries.map { it.label to it.compactLabel },
        selectedLabel = selected.label,
    ) { label ->
        CrushClipCompensation.entries.firstOrNull { it.label == label }?.let(onSelect)
    }
}

@Composable
private fun ScopeGuideRows(guides: ScopeGuides, onChange: (ScopeGuides) -> Unit) {
    SettingsSwitchRow("安全边界削波", isOn = guides.clip, stacked = true) {
        onChange(guides.copy(clip = !guides.clip))
    }
    SettingsSwitchRow("安全边界截止", isOn = guides.crush, stacked = true) {
        onChange(guides.copy(crush = !guides.crush))
    }
    SettingsSwitchRow("中灰", isOn = guides.middle, stacked = true) {
        onChange(guides.copy(middle = !guides.middle))
    }
}

@Composable
private fun ControlsRows(model: AppModel) {
    val view = LocalView.current
    val context = LocalContext.current
    val status by model.session.status.collectAsState()
    var gimbalGamepadStick by remember {
        mutableStateOf(OperatorPrefs.gimbalGamepadStick(context))
    }
    SettingsRowCard(title = "Touch & safety") {
        SettingsSwitchInlineRow(
            title = "Record confirmation",
            help = SettingsHelpCopy.RECORD_CONFIRMATION,
            showTopDivider = false,
            isOn = model.recordConfirmationEnabled,
        ) {
            operatorHaptic(view, model.hapticsEnabled)
            model.updateRecordConfirmationEnabled(!model.recordConfirmationEnabled)
        }
        SettingsSwitchInlineRow(
            title = "触感反馈",
            help = SettingsHelpCopy.HAPTICS,
            isOn = model.hapticsEnabled,
        ) {
            val next = !model.hapticsEnabled
            operatorHaptic(view, model.hapticsEnabled)
            model.updateHapticsEnabled(next)
        }
        SettingsSwitchInlineRow(
            title = "Keep screen awake",
            help = SettingsHelpCopy.KEEP_SCREEN_AWAKE,
            isOn = model.keepScreenAwake,
        ) {
            operatorHaptic(view, model.hapticsEnabled)
            model.updateKeepScreenAwake(!model.keepScreenAwake)
        }
    }
    if (model.monitorCapabilities(status).gimbal) {
        SettingsRowCard(title = "Gimbal") {
            SettingsInlineRow(
                title = "Joystick sensitivity",
                help = SettingsHelpCopy.JOYSTICK_SENSITIVITY,
                showTopDivider = false,
                stacked = true,
            ) {
                SettingsValueSlider(
                    value = model.gimbalStickSensitivity,
                    range = 1..5,
                    label = "${model.gimbalStickSensitivity}",
                    labelWidth = 24,
                    onChange = { next ->
                        if (next != model.gimbalStickSensitivity) {
                            operatorHaptic(view, model.hapticsEnabled)
                            model.updateGimbalStickSensitivity(next)
                        }
                    },
                )
            }
        }
        SettingsRowCard(title = "On-screen joystick") {
            SettingsSwitchInlineRow(
                title = "Invert pan",
                help = SettingsHelpCopy.VIRTUAL_JOYSTICK_INVERT_PAN,
                showTopDivider = false,
                isOn = model.virtualJoystickInvertPan,
                testTag = "gimbal.virtual.invertPan",
            ) {
                operatorHaptic(view, model.hapticsEnabled)
                model.updateVirtualJoystickInvertPan(!model.virtualJoystickInvertPan)
            }
            SettingsSwitchInlineRow(
                title = "Invert tilt",
                help = SettingsHelpCopy.VIRTUAL_JOYSTICK_INVERT_TILT,
                isOn = model.virtualJoystickInvertTilt,
                testTag = "gimbal.virtual.invertTilt",
            ) {
                operatorHaptic(view, model.hapticsEnabled)
                model.updateVirtualJoystickInvertTilt(!model.virtualJoystickInvertTilt)
            }
            SettingsInlineRow(
                title = "Dead zone",
                help = SettingsHelpCopy.VIRTUAL_JOYSTICK_DEADZONE,
                stacked = true,
            ) {
                Box(Modifier.testTag("gimbal.virtual.deadzone")) {
                    SettingsPercentSlider(
                        value = model.virtualJoystickDeadzonePercent,
                        range = 0..25,
                        onChange = { next ->
                            if (next != model.virtualJoystickDeadzonePercent) {
                                operatorHaptic(view, model.hapticsEnabled)
                                model.updateVirtualJoystickDeadzonePercent(next)
                            }
                        },
                    )
                }
            }
            SettingsInlineRow(
                title = "Response curve",
                help = SettingsHelpCopy.VIRTUAL_JOYSTICK_RESPONSE,
                stacked = true,
            ) {
                SettingsSegmented(
                    options = CameraCommands.VirtualJoystickCurve.entries.map { it.label },
                    selected = model.virtualJoystickResponseCurve.label,
                    compact = true,
                    testTag = "gimbal.virtual.response",
                ) { label ->
                    val next = CameraCommands.VirtualJoystickCurve.fromLabel(label)
                    if (next != model.virtualJoystickResponseCurve) {
                        operatorHaptic(view, model.hapticsEnabled)
                        model.updateVirtualJoystickResponseCurve(next)
                    }
                }
            }
        }
    }
    SettingsRowCard(title = "Controller") {
        SettingsInlineRow("Gimbal joystick", SettingsHelpCopy.GIMBAL_JOYSTICK, showTopDivider = false, stacked = true) {
            SettingsSegmented(
                options = GamepadGimbalStick.entries.map { it.label },
                selected = gimbalGamepadStick.label,
                compact = true,
            ) { label ->
                val next = GamepadGimbalStick.fromLabel(label)
                if (next != gimbalGamepadStick) {
                    operatorHaptic(view, model.hapticsEnabled)
                    OperatorPrefs.setGimbalGamepadStick(context, next)
                    gimbalGamepadStick = next
                    model.gimbalGamepad.noteStickSelectionChanged(model)
                }
            }
        }
        SettingsInlineRow("手柄", SettingsHelpCopy.GAMEPAD) {
            SettingsValueText(if (model.gamepadConnected) "已连接" else "未连接")
        }
    }
}

@Composable
private fun DisplayRows(
    model: AppModel,
    isLive: Boolean,
    expandedDisp: PocketDispMode?,
    onExpandDisp: (PocketDispMode?) -> Unit,
) {
    val view = LocalView.current
    PocketDispMode.entries.forEach { mode ->
        SettingsRowCard(
            title = mode.settingsTitle,
            onReset = {
                operatorHaptic(view, model.hapticsEnabled)
                resetDispChrome(model, mode)
            },
        ) {
            Text(
                mode.settingsCaption,
                style = LiveType.ui(10.5f),
                color = LiveDesign.muted,
                modifier = Modifier.padding(vertical = 5.dp),
            )
            DispSectionBody(model, mode, isLive, view)
        }
    }
}

@Composable
private fun DispSectionBody(model: AppModel, mode: PocketDispMode, isLive: Boolean, view: View) {
    if (isLive) {
        SettingsActionPill(
            title = "编辑视图",
            modifier = Modifier.padding(vertical = 8.dp),
            onClick = {
                operatorHaptic(view, model.hapticsEnabled)
                model.homePanel = null
                model.beginChromeEditing(mode)
            },
        )
    } else {
        Text(
            "连接相机后才能在监视器上排列。",
            style = LiveType.ui(11f, FontWeight.SemiBold),
            color = LiveDesign.muted,
            modifier = Modifier.padding(vertical = 6.dp),
        )
    }
    DispToggles(model, mode, view)
    if (mode == PocketDispMode.CLEAN) {
        Text(
            "简洁视图下保持开启的画面辅助",
            style = LiveType.ui(11f, FontWeight.SemiBold),
            color = LiveDesign.muted,
            modifier = Modifier.padding(top = 8.dp, bottom = 6.dp),
        )
        CleanViewPinStrip(model, view)
    }
}

private data class DispToggleSpec(val section: PocketDispSection, val title: String, val help: String)

private val dispToggleSpecs =
    listOf(
        DispToggleSpec(
            PocketDispSection.STATUS_BAR,
            "状态栏",
            "画面顶部的录制、时间码、格式和 FPS。",
        ),
        DispToggleSpec(PocketDispSection.TOOL_BAR, "工具栏", "画面下方的辅助工具条。"),
        DispToggleSpec(
            PocketDispSection.CAMERA_VALUES,
            "相机数值",
            "ISO、快门、白平衡等整条拍摄控制栏。",
        ),
        DispToggleSpec(
            PocketDispSection.LOCK_BUTTON,
            "锁定按钮",
            "侧栏锁定。界面锁定时仍可重新挂载。",
        ),
        DispToggleSpec(PocketDispSection.BATTERIES, "电量", "手机与相机电量组件。"),
        DispToggleSpec(PocketDispSection.REC_READOUT, "REC", "状态栏上的待机/录制指示。"),
        DispToggleSpec(PocketDispSection.TIMECODE, "时间码", "状态栏上的运行时间码。"),
        DispToggleSpec(PocketDispSection.FORMAT, "格式", "录制分辨率与帧率。"),
        DispToggleSpec(PocketDispSection.COLOR, "色彩", "状态栏上的色彩模式。"),
        DispToggleSpec(PocketDispSection.STORAGE, "存储", "状态栏上的剩余可录时长。"),
        DispToggleSpec(PocketDispSection.FPS, "FPS", "实时画面帧率与信号条。"),
        DispToggleSpec(PocketDispSection.RAIL_RECORD, "录制", "侧栏录制灯。录制中仍可操作。"),
        DispToggleSpec(PocketDispSection.RAIL_MEDIA, "媒体", "侧栏媒体按钮。"),
        DispToggleSpec(PocketDispSection.RAIL_SETTINGS, "设置", "侧栏设置按钮。随时可用。"),
        DispToggleSpec(PocketDispSection.ZOOM_CHIP, "变焦指示", "画面上的变焦读数。"),
        DispToggleSpec(PocketDispSection.GIMBAL_STICK, "云台摇杆", "屏幕云台摇杆。"),
        DispToggleSpec(PocketDispSection.FOCUS_BOX, "对焦框", "画面上的对焦与人脸跟踪框。"),
    )

@Composable
private fun DispToggles(model: AppModel, mode: PocketDispMode, view: View) {
    val chrome = model.chrome(mode)
    val status by model.session.status.collectAsState()
    dispToggleSpecs.filter {
        it.section != PocketDispSection.GIMBAL_STICK || model.monitorCapabilities(status).gimbal
    }.forEach { spec ->
        SettingsSwitchInlineRow(
            title = spec.title,
            help = spec.help,
            showTopDivider = true,
            isOn = chrome.isVisible(spec.section),
        ) {
            operatorHaptic(view, model.hapticsEnabled)
            model.toggleChrome(spec.section, mode)
        }
    }
}

@Composable
private fun CleanViewPinStrip(model: AppModel, view: View) {
    val assist = model.assist
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        CleanPinTool.entries.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                row.forEach { tool ->
                    val on = assist.pinned.any { it.name == tool.key }
                    DisplayToggleItem(
                        title = tool.title,
                        isOn = on,
                        modifier = Modifier.weight(1f),
                        onToggle = {
                            operatorHaptic(view, model.hapticsEnabled)
                            val next = toggledCleanPins(assist.pinned.map { it.name }.toSet(), tool.key)
                            assist.pinned =
                                next.mapNotNull(LiveAssistTool::fromPersisted).toSet()
                            model.updateCleanViewPinnedTools(next)
                        },
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StorageRows(model: AppModel, onClearCache: () -> Unit) {
    val context = LocalContext.current
    val view = LocalView.current
    val bytes = OperatorMediaCache.byteCount(context)
    val cacheLabel =
        if (OperatorMediaCache.existingDir(context) == null) "空" else formatCacheSize(bytes)
    SettingsRowCard {
        SettingsInlineRow("Frame.io", SettingsHelpCopy.FRAME_IO, showTopDivider = false) {
            SettingsValueText("未配置")
        }
    }
    SettingsRowCard {
        SettingsSwitchInlineRow(
            title = "全分辨率缓存",
            isOn = model.cacheFullResolution,
            help = SettingsHelpCopy.CACHE_FULL_RESOLUTION,
            showTopDivider = false,
        ) {
            operatorHaptic(view, model.hapticsEnabled)
            model.updateCacheFullResolution(!model.cacheFullResolution)
        }
        SettingsInlineRow("本地媒体缓存", SettingsHelpCopy.LOCAL_CACHE) {
            SettingsValueText(cacheLabel)
        }
        SettingsInlineRow("清除缓存", SettingsHelpCopy.CLEAR_CACHE) {
            Text(
                "清除",
                style = LiveType.ui(13f, FontWeight.SemiBold),
                color = LiveDesign.rec,
                modifier = Modifier.settingsClickable(role = Role.Button, onClick = onClearCache),
            )
        }
    }
}

@Composable
private fun SystemRows(model: AppModel, onLegal: (LegalKind) -> Unit) {
    val context = LocalContext.current
    val view = LocalView.current
    var reliabilityOptIn by remember { mutableStateOf(ReliabilityReporting.isOptedIn) }
    var diagnosticOptions by remember { mutableStateOf(false) }
    var showReportForm by remember { mutableStateOf(false) }
    val report by ManualProblemReport.snapshot.collectAsState()
    SettingsRowCard(title = "帮助与反馈") {
        SettingsInlineRow("Report a problem",
            SettingsHelpCopy.REPORT_PROBLEM,
            showTopDivider = false) {
            SettingsActionPill("Open") { showReportForm = true }
        }
        if (report.delivery != ManualProblemReportDelivery.IDLE) {
            SettingsInlineRow(
                "Report status",
                report.detail ?: "This phone keeps one report until Sentry accepts it or you discard it.",
            ) {
                SettingsValueText(report.statusLabel)
            }
            report.eventId?.let { id ->
                SettingsInlineRow("Report ID", "Use this ID if you follow up.") {
                    SettingsValueText(id)
                }
            }
            if (report.allowsDiscard) {
                SettingsInlineRow("Queued report", "Remove the unsent report from this phone.") {
                    SettingsActionPill("Discard") { ManualProblemReport.discard() }
                }
            }
        }
        if (ReliabilityReporting.isAvailable) {
            SettingsSwitchInlineRow(
                title = "Automatic error reports",
                help = SettingsHelpCopy.RELIABILITY_REPORTS,
                isOn = reliabilityOptIn,
            ) {
                operatorHaptic(view, model.hapticsEnabled)
                reliabilityOptIn = !reliabilityOptIn
                ReliabilityReporting.setConsent(reliabilityOptIn)
            }
        } else {
            SettingsInlineRow(
                title = "Automatic error reports",
                help = SettingsHelpCopy.RELIABILITY_UNAVAILABLE,
            ) {
                SettingsValueText("Off")
            }
        }
        SettingsInlineRow("Reporting Privacy", "What reports contain, retention, and how to request deletion.") {
            SettingsActionPill("Read") { onLegal(LegalKind.PRIVACY) }
        }
    }
    SettingsGroupCard(
        title = "Diagnostic options",
        caption = "Save or remove reports on this phone",
        expanded = diagnosticOptions,
        onExpandToggle = { diagnosticOptions = !diagnosticOptions },
    ) {
        SettingsInlineRow("Save diagnostic report", "Keep a copy or share it with support.", showTopDivider = false) {
            SettingsActionPill("Save") { DiagnosticCenter.shareReport(context, model.session, copyForFeedback = false) }
        }
        SettingsInlineRow("Delete saved feed reports",
            "Remove saved feed reports from this phone. Connection logs remain. Turn off automatic reporting to clear pending uploads. Reports already sent cannot be removed here.") {
            SettingsActionPill("Delete") {
                com.opencapture.openpocketcine.diagnostics.FeedIncidentRuntime.deleteStoredReports { deleted ->
                    android.widget.Toast.makeText(context,
                        if (deleted) "Saved feed reports deleted" else "Couldn't delete saved feed reports. Please try again.",
                        android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    if (showReportForm) {
        ManualProblemReportDialog(
            model = model,
            onClose = { showReportForm = false },
            onPrivacy = {
                showReportForm = false
                onLegal(LegalKind.PRIVACY)
            },
        )
    }

    SettingsRowCard(title = "项目与法律信息") {
        SettingsInlineRow("源代码", SettingsHelpCopy.SOURCE, showTopDivider = false) {
            SettingsActionPill("打开") { openUrl(context, OpenPocketCineLinks.SOURCE) }
        }
        SettingsInlineRow("隐私", "本应用在此手机上保存了什么。") {
            SettingsActionPill("Open") { onLegal(LegalKind.PRIVACY) }
        }
        SettingsInlineRow("条款", "OpenPocketCine 的使用许可说明。") {
            SettingsActionPill("打开") { openUrl(context, OpenPocketCineLinks.TERMS) }
        }
        SettingsInlineRow("许可证", "Apache 2.0 与第三方声明。") {
            SettingsActionPill("打开") { onLegal(LegalKind.LICENSES) }
        }
        SettingsInlineRow("声明", "随应用内置的署名信息。") {
            SettingsActionPill("打开") { onLegal(LegalKind.NOTICE) }
        }
    }
    SettingsRowCard(title = "应用信息") {
        SettingsInlineRow("主题", SettingsHelpCopy.THEME, showTopDivider = false) {
            SettingsValueText("DJI 黑")
        }
        SettingsInlineRow("协议实现", SettingsHelpCopy.PROTOCOL) {
            SettingsValueText("DUML / 蓝牙 + Wi-Fi")
        }
        SettingsInlineRow("应用版本", SettingsHelpCopy.APP_VERSION) {
            SettingsValueText(formatAppVersion(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE.toLong()))
        }
    }
}

internal fun openUrl(context: Context, url: String) {
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
}

private fun operatorHaptic(view: View, enabled: Boolean) {
    if (!enabled) return
    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
}

@Composable
fun OperatorCloseButton(onClose: () -> Unit, modifier: Modifier = Modifier) {
    PanelCloseButton(onClick = onClose, modifier = modifier)
}

@Composable
private fun SettingsSessionStatus(
    isLive: Boolean,
    phaseLabel: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(10.dp))
            .padding(9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            Modifier
                .size(7.dp)
                .background(if (isLive) LiveDesign.good else LiveDesign.faint, CircleShape),
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                if (isLive) "Active link" else "No camera connected",
                style = LiveType.ui(11.5f, FontWeight.SemiBold),
                color = LiveDesign.text,
                maxLines = 1,
            )
            Text(
                phaseLabel,
                style = LiveType.ui(9f),
                color = LiveDesign.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SettingsLiveTile(
    isLive: Boolean,
    phaseLabel: String,
    bars: Int,
    cameraName: String,
    fpsLabel: String,
    hasVideoFormat: Boolean,
) {
    val tint =
        when {
            !isLive -> LiveDesign.faint
            bars >= 3 -> LiveDesign.good
            bars == 2 -> LiveDesign.accent
            bars == 1 -> LiveDesign.rec
            hasVideoFormat -> LiveDesign.accent
            else -> LiveDesign.faint
        }
    val lit =
        when {
            !isLive -> 0
            bars > 0 -> bars.coerceIn(0, 4)
            hasVideoFormat -> 2
            else -> 1
        }
    val detail = OperatorLinkHealth.liveTileDetail(isLive, cameraName, fpsLabel, phaseLabel)
    Row(
        Modifier
            .background(LiveDesign.surface, ChromeShape)
            .border(1.dp, LiveDesign.hairline, ChromeShape)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.size(8.dp).background(tint, CircleShape))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                if (isLive) "活动连接" else "无连接",
                style = LiveType.ui(12f, FontWeight.SemiBold),
                color = LiveDesign.text,
                maxLines = 1,
            )
            Text(
                detail,
                style = LiveType.mono(10.5f),
                color = LiveDesign.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.Bottom) {
            repeat(4) { index ->
                Box(
                    Modifier
                        .width(3.dp)
                        .height((6 + index * 3).dp)
                        .background(
                            if (index < lit) tint.copy(alpha = 0.52f + index * 0.12f) else LiveDesign.hairline,
                            CircleShape,
                        ),
                )
            }
        }
    }
}

@Composable
private fun ScrollMoreCue(modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(
                Brush.verticalGradient(listOf(LiveDesign.surface.copy(alpha = 0f), LiveDesign.surface)),
            )
            .padding(bottom = 13.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        Text(
            "更多",
            style = LiveType.mono(9.5f, FontWeight.Bold).copy(letterSpacing = 1.2.sp, color = LiveDesign.muted),
        )
        OpcIcon(
            icon = OpcIcon.CHEVRON_DOWN,
            contentDescription = null,
            tint = LiveDesign.muted,
            modifier = Modifier.size(10.dp),
        )
    }
}
