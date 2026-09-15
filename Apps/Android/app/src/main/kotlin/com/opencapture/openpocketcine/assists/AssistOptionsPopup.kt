package com.opencapture.openpocketcine.assists

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opencapture.openpocketcine.AppModel
import com.opencapture.openpocketcine.LiveDesign
import com.opencapture.openpocketcine.NDFilterNotation
import com.opencapture.openpocketcine.LivePopupCloseButton
import com.opencapture.openpocketcine.LiveType
import com.opencapture.openpocketcine.LocalOperatorHaptics
import com.opencapture.openpocketcine.OperatorPrefs
import com.opencapture.openpocketcine.chromeClickable
import com.opencapture.openpocketcine.feed.MonitorTransfer
import com.opencapture.openpocketcine.lut.LUTPicker
import com.opencapture.openpocketcine.lut.LUTSplitComparisonBar
import com.opencapture.openpocketcine.pickerPanelGlass
import com.opencapture.openpocketcine.session.CameraCommands
import com.opencapture.openpocketcine.settings.SettingsColorDots
import com.opencapture.openpocketcine.settings.SettingsCrushClipSegmented
import com.opencapture.openpocketcine.settings.SettingsInlineRow
import com.opencapture.openpocketcine.settings.SettingsNumberField
import com.opencapture.openpocketcine.settings.SettingsPalette
import com.opencapture.openpocketcine.settings.SettingsPercentSlider
import com.opencapture.openpocketcine.settings.SettingsSegmented
import com.opencapture.openpocketcine.settings.SettingsSwitchGraphic
import com.opencapture.openpocketcine.settings.SettingsSwitchInlineRow

private val CardShape = RoundedCornerShape(LiveDesign.CORNER_RADIUS_DP.dp)

@Composable
private fun Modifier.assistClick(onClick: () -> Unit): Modifier = chromeClickable(onClick = onClick)

/**
 * OpenZCine / iOS long-press tray. Overlay glass, not the feed-layer pill.
 *
 * [model] owns the live LUT selection. When omitted (current live-view call
 * site), the picker writes [OperatorPrefs] so a later process still restores.
 */
@Composable
fun AssistOptionsPopup(
    tool: LiveAssistTool,
    state: LiveAssistState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    model: AppModel? = null,
    maxHeightDp: Float? = null,
    colorMode: Int = CameraCommands.COLOR_NORMAL,
) {
    val width = AssistLongPress.preferredWidthDp(tool).dp
    val context = LocalContext.current
    var fallbackLut by remember {
        mutableStateOf(model?.lutSelection ?: OperatorPrefs.lutSelection(context))
    }
    val lutSelection = model?.lutSelection ?: fallbackLut
    val cap = maxHeightDp?.dp
    val isLut = tool == LiveAssistTool.LUT
    val panelPad = AssistLongPress.PANEL_PAD_DP.dp
    val panelGap = AssistLongPress.PANEL_GAP_DP.dp
    Column(
        modifier
            .widthIn(max = width)
            .width(width)
            .then(
                if (isLut && cap != null) {
                    Modifier.height(cap)
                } else {
                    Modifier.wrapContentHeight(align = Alignment.Top)
                        .then(if (cap != null) Modifier.heightIn(max = cap) else Modifier)
                },
            )
            .pickerPanelGlass(CardShape)
            .padding(panelPad),
        verticalArrangement = Arrangement.spacedBy(panelGap),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AssistToolGlyph(tool, LiveDesign.text, Modifier.size(15.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                tool.title.uppercase(),
                style = LiveType.ui(15f, FontWeight.Bold).copy(letterSpacing = 1.2.sp),
                color = LiveDesign.text,
            )
            Spacer(Modifier.weight(1f))
            LivePopupCloseButton(
                onClick = onDismiss,
                size = AssistLongPress.CLOSE_DP.dp,
            )
        }
        if (isLut) {
            // iOS pins 50/50 under the catalog. Weight the picker so a short
            // landscape well scrolls the drum instead of clipping the footer.
            Box(
                Modifier
                    .weight(1f, fill = true)
                    .fillMaxWidth(),
            ) {
                LUTPicker(
                    selection = lutSelection,
                    onSelect = { id ->
                        if (model != null) {
                            model.updateLutSelection(id)
                        } else {
                            fallbackLut = id
                            OperatorPrefs.setLutSelection(context, id)
                        }
                        state.armLut()
                    },
                    embedded = true,
                    splitComparison = state.splitComparison,
                    splitVertical = state.splitVertical,
                    lutExposureStops = state.lutExposureStops,
                    onToggleSplit = { state.setSplitComparison(!state.splitComparison) },
                    onSplitVertical = { state.setSplitComparison(state.splitComparison, it) },
                    onNudgeExposure = { state.nudgeLutExposure(it) },
                    onArmLut = { state.armLut() },
                    colorMode = colorMode,
                    family = model?.session?.connectedCamera?.model?.family ?: "pocket",
                    cameraName = model?.session?.connectedCamera?.name,
                )
            }
        } else {
            Column(
                Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth()
                    .then(if (cap != null) Modifier.verticalScroll(rememberScrollState()) else Modifier),
            ) {
                AssistOptionsBody(tool, state, colorMode)
            }
        }
        if (isLut) {
            LUTSplitComparisonBar(
                splitComparison = state.splitComparison,
                splitVertical = state.splitVertical,
                lutExposureStops = state.lutExposureStops,
                onToggleSplit = { state.setSplitComparison(!state.splitComparison) },
                onSplitVertical = { state.setSplitComparison(state.splitComparison, it) },
                onNudgeExposure = { state.nudgeLutExposure(it) },
            )
        }
    }
}

@Composable
private fun AssistOptionsBody(tool: LiveAssistTool, state: LiveAssistState, colorMode: Int) {
    when (tool) {
        LiveAssistTool.LUT -> Spacer(Modifier.height(0.dp))
        LiveAssistTool.PEAK -> PeakingOptions(state)
        LiveAssistTool.FALSE -> FalseColorOptions(state)
        LiveAssistTool.ZEBRA -> ZebraOptions(state, colorMode)
        LiveAssistTool.WAVE -> WaveformOptions(state)
        LiveAssistTool.PARADE -> ParadeOptions(state)
        LiveAssistTool.HISTO -> HistogramOptions(state)
        LiveAssistTool.VECTOR -> VectorscopeOptions(state)
        LiveAssistTool.LIGHTS -> LightsOptions(state)
        LiveAssistTool.ND -> NdOptions(state)
        LiveAssistTool.GUIDES -> GuidesOptions(state)
        LiveAssistTool.GRID -> GridOptions(state)
        LiveAssistTool.CROSS -> OptionCopy(CrosshairAssist.HELP)
        LiveAssistTool.MIRROR -> OptionCopy(MirrorAssist.EXPLANATION)
        LiveAssistTool.AUDIO -> OptionCopy(AudioAssist.HELP)
    }
}

@Composable
private fun PeakingOptions(state: LiveAssistState) {
    val haptics = LocalOperatorHaptics.current
    SettingsInlineRow("灵敏度", help = "灵敏度越高，越能捕捉细微边缘，但细节丰富的画面可能出现噪点。", showTopDivider = false, stacked = true) {
        SettingsSegmented(
            options = PeakingSense.entries.map { it.label },
            selected = state.peakingSensitivity.label,
        ) { label ->
            haptics.selection()
            state.setPeaking(sense = PeakingSense.fromPersisted(label))
        }
    }
    SettingsInlineRow("颜色", help = "选择在常见场景上依然醒目的描边颜色。", stacked = true) {
        SettingsColorDots(
            dots = SettingsPalette.peaking,
            selectedName = state.peakingColor.label,
        ) { label ->
            haptics.selection()
            state.setPeaking(color = PeakingColor.fromPersisted(label))
        }
    }
}

@Composable
private fun FalseColorOptions(state: LiveAssistState) {
    val haptics = LocalOperatorHaptics.current
    SettingsInlineRow(
        "标尺",
        help =
            "相机色彩模式会自动选择 D-Log、D-Log2、D-Log M、Rec.709 或 HLG。" +
                "CineStop 会在亮度灰阶上涂视频电平条纹。EL Zone " +
                "从 18% 灰起涂 15 个连续档位：+6 以上为白、−6 以下为黑。" +
                "IRE 在亮度灰阶上涂六个视频电平区间。限值只涂阴影和高光警告。" +
                "D-Log M 使用 0–100 直接信号刻度，它的 EL Zone 和中灰参考是" +
                "基于 Pocket 3 的估算值，不是标定过的传感器极限。其他 D-Log M 机型请用 IRE。",
        showTopDivider = false,
        stacked = true,
    ) {
        SettingsSegmented(
            options = listOf("CineStop 档位", "EL Zone 曝光", "IRE 电平", "限值"),
            selected = state.falseColorScale.menuLabel,
        ) { label ->
            haptics.selection()
            state.setFalseColor(scale = FalseColorScale.fromMenuLabel(label))
        }
    }
    SettingsSwitchInlineRow(
        title = "基准显示器",
        isOn = state.falseColorReference,
        help = "伪色开启时，在实时画面上显示小型颜色对照表。",
        stacked = true,
    ) {
        haptics.selection()
        state.setFalseColor(reference = !state.falseColorReference)
    }
}

@Composable
private fun ZebraOptions(state: LiveAssistState, colorMode: Int) {
    val haptics = LocalOperatorHaptics.current
    val transfer = MonitorTransfer.fromColorMode(colorMode)
    val maximum = ZebraEditor.editorMaximum(state.zebraUnit)
    SettingsInlineRow(
        "单位",
        help = "在原生 0-255 编码值和 0-100 监看 IRE 刻度间切换。",
        showTopDivider = false,
        stacked = true,
    ) {
        SettingsSegmented(
            options = listOf("0-255", "IRE"),
            selected = state.zebraUnit.editorLabel,
        ) { label ->
            haptics.selection()
            state.updateZebraUnit(ZebraUnit.fromEditorLabel(label))
        }
    }
    ZebraZoneRow(
        title = "高光",
        help = "高斑马线：在补偿当前 log 曲线后，高光细节接近削波时警告。",
        enabled = state.zebraHighlight,
        value = ZebraEditor.displayValue(state.zebraHighlightIRE, state.zebraUnit, transfer),
        maximum = maximum,
        selected = state.zebraHighlightColor.label,
        palette = SettingsPalette.highlight,
        onEnabled = {
            haptics.selection()
            state.setZebraHighlight(enabled = !state.zebraHighlight)
        },
        onValue = {
            state.setZebraHighlight(ire = ZebraEditor.ireFromDisplay(it, state.zebraUnit, transfer))
        },
        onColor = {
            haptics.selection()
            state.setZebraHighlight(color = ZebraPaint.fromPersisted(it))
        },
    )
    ZebraZoneRow(
        title = "中间调",
        help = "中斑马线：给出经曲线补偿的参考带，用于人脸或主体曝光。",
        enabled = state.zebraMidtone,
        value = ZebraEditor.displayValue(state.zebraMidtoneIRE, state.zebraUnit, transfer),
        maximum = maximum,
        selected = state.zebraMidtoneColor.label,
        palette = SettingsPalette.midtone,
        onEnabled = {
            haptics.selection()
            state.setZebraMidtone(enabled = !state.zebraMidtone)
        },
        onValue = {
            state.setZebraMidtone(ire = ZebraEditor.ireFromDisplay(it, state.zebraUnit, transfer))
        },
        onColor = {
            haptics.selection()
            state.setZebraMidtone(color = ZebraPaint.fromPersisted(it))
        },
    )
}

@Composable
private fun ZebraZoneRow(
    title: String,
    help: String,
    enabled: Boolean,
    value: Int,
    maximum: Int,
    selected: String,
    palette: List<com.opencapture.openpocketcine.settings.SettingsColorDot>,
    onEnabled: () -> Unit,
    onValue: (Int) -> Unit,
    onColor: (String) -> Unit,
) {
    SettingsInlineRow(title = title, help = help, stacked = true) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(Modifier.chromeClickable(onClick = onEnabled).semantics { role = Role.Switch }) {
                SettingsSwitchGraphic(isOn = enabled)
            }
            SettingsNumberField(value = value.coerceIn(0, maximum), maximum = maximum, onChange = onValue)
            Spacer(Modifier.weight(1f))
            SettingsColorDots(dots = palette, selectedName = selected, onSelect = onColor)
        }
    }
}

@Composable
private fun WaveformOptions(state: LiveAssistState) {
    val haptics = LocalOperatorHaptics.current
    SettingsInlineRow("模式", showTopDivider = false, stacked = true) {
        SettingsSegmented(options = listOf("亮度", "RGB"), selected = state.waveMode.label) {
            haptics.selection()
            state.setWaveform(mode = WaveformMode.fromPersisted(it))
        }
    }
    SettingsInlineRow("亮度", help = "强光下波形难读时，提高轨迹亮度。", stacked = true) {
        SettingsPercentSlider(value = state.waveBrightness, range = 0..200) {
            state.setWaveform(brightness = it)
        }
    }
    GuideToggles(state.waveGuides) {
        haptics.selection()
        state.setWaveform(guides = it)
    }
}

@Composable
private fun ParadeOptions(state: LiveAssistState) {
    val haptics = LocalOperatorHaptics.current
    SettingsInlineRow("模式", showTopDivider = false, stacked = true) {
        SettingsSegmented(options = listOf("RGB", "YRGB"), selected = state.paradeMode.label) {
            haptics.selection()
            state.setParade(mode = ParadeMode.fromPersisted(it))
        }
    }
    SettingsInlineRow("亮度", help = "通道分离难辨认时，提高轨迹亮度。", stacked = true) {
        SettingsPercentSlider(value = state.paradeBrightness, range = 0..200) {
            state.setParade(brightness = it)
        }
    }
    GuideToggles(state.paradeGuides) {
        haptics.selection()
        state.setParade(guides = it)
    }
}

@Composable
private fun HistogramOptions(state: LiveAssistState) {
    val haptics = LocalOperatorHaptics.current
    SettingsSwitchInlineRow(
        title = HistogramAssist.TRAFFIC_LIGHTS_TITLE,
        isOn = state.histoTrafficLights,
        help = HistogramAssist.TRAFFIC_LIGHTS_HELP,
        showTopDivider = false,
        stacked = true,
    ) {
        haptics.selection()
        state.setHistogram(traffic = !state.histoTrafficLights)
    }
    SettingsInlineRow(HistogramAssist.COMPENSATION_TITLE, HistogramAssist.COMPENSATION_HELP, stacked = true) {
        CompensationPicker(state.crushClipCompensation) {
            haptics.selection()
            state.setHistogram(compensation = it)
        }
    }
}

@Composable
private fun VectorscopeOptions(state: LiveAssistState) {
    val haptics = LocalOperatorHaptics.current
    SettingsInlineRow(
        "轨迹放大",
        help = "只放大色度轨迹，刻度线保持不变。",
        showTopDivider = false,
        stacked = true,
    ) {
        SettingsSegmented(
            options = VectorscopeZoom.entries.map { it.label },
            selected = state.vectorZoom.label,
        ) {
            haptics.selection()
            state.setVectorscope(zoom = VectorscopeZoom.fromPersisted(it))
        }
    }
    SettingsInlineRow("亮度", help = "色度图难读时，提高轨迹亮度。", stacked = true) {
        SettingsPercentSlider(value = state.vectorBrightness, range = 0..200) {
            state.setVectorscope(brightness = it)
        }
    }
}

@Composable
private fun LightsOptions(state: LiveAssistState) {
    val haptics = LocalOperatorHaptics.current
    SettingsInlineRow(
        HistogramAssist.COMPENSATION_TITLE,
        help = "通道指示灯点亮前的暗部截止/高光削波容差档数。与直方图红绿灯共用。",
        showTopDivider = false,
        stacked = true,
    ) {
        CompensationPicker(state.crushClipCompensation) {
            haptics.selection()
            state.setCompensation(it)
        }
    }
}

@Composable
private fun NdOptions(state: LiveAssistState) {
    val haptics = LocalOperatorHaptics.current
    SettingsInlineRow(
        NDAssist.NOTATION_TITLE,
        help = NDAssist.NOTATION_HELP,
        showTopDivider = false,
        stacked = true,
    ) {
        SettingsSegmented(
            options = NDFilterNotation.entries.map { it.editorLabel },
            selected = state.ndNotation.editorLabel,
        ) { label ->
            haptics.selection()
            state.ndNotation = NDFilterNotation.fromEditorLabel(label)
            state.persist()
        }
    }
    OptionCopy(NDAssist.HELP)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GuidesOptions(state: LiveAssistState) {
    val haptics = LocalOperatorHaptics.current
    SettingsSegmented(
        options = GuideFamily.entries.map { it.label },
        selected = state.guideFamily.label,
    ) {
        haptics.selection()
        state.updateGuideFamily(GuideFamily.fromPersisted(it))
    }
    Spacer(Modifier.height(10.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        GuideAspect.ratios(state.guideFamily).forEach { aspect ->
            val on = aspect in state.selectedGuides
            Text(
                aspect.label,
                color = if (on) LiveDesign.accent else LiveDesign.text,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                modifier =
                    Modifier
                        .clip(CardShape)
                        .background(if (on) LiveDesign.accentDim else LiveDesign.glassBright)
                        .border(1.dp, if (on) LiveDesign.accentDim else LiveDesign.hairline, CardShape)
                        .assistClick {
                            haptics.selection()
                            state.toggleGuide(aspect)
                        }
                        .padding(horizontal = 10.dp, vertical = 12.dp),
            )
        }
    }
    Spacer(Modifier.height(10.dp))
    SettingsSwitchInlineRow("画幅外遮罩", isOn = state.guideMask, showTopDivider = false, stacked = true) {
        haptics.selection()
        state.updateGuideMask(!state.guideMask)
    }
}

@Composable
private fun GridOptions(state: LiveAssistState) {
    val haptics = LocalOperatorHaptics.current
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        GridAssist.optionLabels.forEach { label ->
            val on =
                when (label) {
                    "三分线" -> state.gridThirds
                    "φ 网格" -> state.gridPhi
                    else -> state.gridDiagonal
                }
            Text(
                label,
                color = if (on) LiveDesign.accent else LiveDesign.text,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                modifier =
                    Modifier
                        .weight(1f)
                        .clip(CardShape)
                        .background(if (on) LiveDesign.accentDim else LiveDesign.glassBright)
                        .border(1.dp, if (on) LiveDesign.accentDim else LiveDesign.hairline, CardShape)
                        .assistClick {
                            haptics.selection()
                            when (label) {
                                "三分线" -> state.setGridOption(thirds = !state.gridThirds)
                                "φ 网格" -> state.setGridOption(phi = !state.gridPhi)
                                else -> state.setGridOption(diagonal = !state.gridDiagonal)
                            }
                        }
                        .padding(vertical = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Composable
private fun GuideToggles(guides: ScopeGuides, onChange: (ScopeGuides) -> Unit) {
    SettingsSwitchInlineRow("安全边界削波", isOn = guides.clip, stacked = true) {
        onChange(guides.copy(clip = !guides.clip))
    }
    SettingsSwitchInlineRow("安全边界截止", isOn = guides.crush, stacked = true) {
        onChange(guides.copy(crush = !guides.crush))
    }
    SettingsSwitchInlineRow("中灰", isOn = guides.middle, stacked = true) {
        onChange(guides.copy(middle = !guides.middle))
    }
}

@Composable
private fun CompensationPicker(selected: CrushClipCompensation, onSelect: (CrushClipCompensation) -> Unit) {
    SettingsCrushClipSegmented(
        options = CrushClipCompensation.entries.map { it.label to it.compactLabel },
        selectedLabel = selected.label,
    ) { label ->
        CrushClipCompensation.entries.firstOrNull { it.label == label }?.let(onSelect)
    }
}

@Composable
private fun OptionCopy(text: String) {
    Text(text, color = LiveDesign.muted, fontSize = 13.sp)
}
