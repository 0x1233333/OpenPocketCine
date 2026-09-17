package com.opencapture.openpocketcine.assists

/**
 * OpenZCine cinema live-monitor set. Pocket omits LEVEL, DE-SQ, MAG, EV, PLAY.
 *
 * Toolbar: LUT PEAK FALSE | ZEBRA WAVE PARADE | HISTO VECTOR LIGHTS ND |
 * GUIDES GRID CROSS | MIRROR | AUDIO.
 */
enum class LiveAssistTool {
    LUT,
    PEAK,
    FALSE,
    ZEBRA,
    WAVE,
    PARADE,
    HISTO,
    VECTOR,
    LIGHTS,
    ND,
    AUDIO,
    GUIDES,
    GRID,
    CROSS,
    MIRROR,
    ;

    val chipLabel: String
        get() = name

    val label: String
        get() = name

    val title: String
        get() =
            when (this) {
                LUT -> "LUT 调色"
                PEAK -> "峰值对焦"
                FALSE -> "伪色"
                ZEBRA -> "斑马纹"
                WAVE -> "波形图"
                PARADE -> "RGB 分量图"
                HISTO -> "直方图"
                VECTOR -> "矢量示波器"
                LIGHTS -> "红绿灯提示"
                ND -> "ND 建议"
                AUDIO -> "音频电平"
                GUIDES -> "参考线"
                GRID -> "网格"
                CROSS -> "十字线"
                MIRROR -> "镜像"
            }

    /** Audio exposes monitor orientation; mirror has no H/V-flip submenu. */
    val hasConfiguration: Boolean
        get() =
            when (this) {
                MIRROR -> false
                else -> true
            }

    companion object {
        val toolbarGroups: List<List<LiveAssistTool>> =
            listOf(
                listOf(LUT, PEAK, FALSE),
                listOf(ZEBRA, WAVE, PARADE),
                listOf(HISTO, VECTOR, LIGHTS, ND),
                listOf(GUIDES, GRID, CROSS),
                listOf(MIRROR),
            )

        /** AUDIO is appended as its own trailing section. */
        val toolbarCases: List<LiveAssistTool> = toolbarGroups.flatten()

        /** Playback drops nothing Pocket already omits; AUDIO rides last like live. */
        val playbackToolbarCases: List<LiveAssistTool> = toolbarCases + AUDIO

        val settingsCases: List<LiveAssistTool> = toolbarCases + AUDIO

        val cleanPinCases: List<LiveAssistTool> = settingsCases

        fun fromPersisted(raw: String): LiveAssistTool? =
            entries.firstOrNull { it.name == raw || it.chipLabel == raw }
    }
}

enum class GuideFamily(val label: String) {
    FILM("电影"),
    SOCIAL("社交"),
    ;

    companion object {
        fun fromPersisted(raw: String): GuideFamily =
            entries.firstOrNull { it.label == raw || it.name == raw } ?: FILM
    }
}

enum class GuideAspect(val label: String) {
    CINEMA_276("2.76:1"),
    CINEMA("2.39:1"),
    CINEMA_235("2.35:1"),
    TWO_K("2.00:1"),
    WIDE("1.85:1"),
    HD("16:9"),
    EURO("1.66:1"),
    IMAX("1.43:1"),
    ACADEMY("4:3"),
    VERTICAL("9:16"),
    SOCIAL("4:5"),
    SQUARE("1:1"),
    PORTRAIT("2:3"),
    FEED("1.91:1"),
    ;

    val ratio: Float
        get() {
            val parts = label.split(':')
            val a = parts.getOrNull(0)?.toFloatOrNull() ?: return 1f
            val b = parts.getOrNull(1)?.toFloatOrNull() ?: return 1f
            return if (b > 0f) a / b else 1f
        }

    companion object {
        val film: List<GuideAspect> =
            listOf(CINEMA_276, CINEMA, CINEMA_235, TWO_K, WIDE, HD, EURO, IMAX, ACADEMY)
        val social: List<GuideAspect> = listOf(VERTICAL, SOCIAL, SQUARE, PORTRAIT, HD, FEED)

        fun ratios(family: GuideFamily): List<GuideAspect> =
            when (family) {
                GuideFamily.FILM -> film
                GuideFamily.SOCIAL -> social
            }

        fun fromPersisted(raw: String): GuideAspect =
            entries.firstOrNull { it.label == raw || it.name == raw } ?: CINEMA
    }
}

enum class PeakingColor(val label: String) {
    WHITE("白色"),
    BLUE("蓝色"),
    RED("红色"),
    GREEN("绿色"),
    ;

    /** Overlay RGB OpenZCine paints on focused edges. */
    val rgb: Triple<Double, Double, Double>
        get() =
            when (this) {
                WHITE -> Triple(246.0 / 255, 241.0 / 255, 226.0 / 255)
                BLUE -> Triple(64.0 / 255, 142.0 / 255, 255.0 / 255)
                RED -> Triple(255.0 / 255, 72.0 / 255, 64.0 / 255)
                GREEN -> Triple(74.0 / 255, 220.0 / 255, 132.0 / 255)
            }

    companion object {
        fun fromPersisted(raw: String): PeakingColor =
            entries.firstOrNull { it.label == raw || it.name == raw } ?: RED
    }
}

enum class PeakingSense(val label: String) {
    LOW("低"),
    MED("中"),
    HIGH("高"),
    ;

    val ratioThreshold: Double
        get() =
            when (this) {
                LOW -> 2.30
                MED -> 2.10
                HIGH -> 1.90
            }

    val noiseGate: Double
        get() =
            when (this) {
                LOW -> 0.00522
                MED -> 0.00174
                HIGH -> 0.00058
            }

    companion object {
        fun fromPersisted(raw: String): PeakingSense =
            entries.firstOrNull { it.label == raw || it.name == raw } ?: MED
    }
}

enum class FalseColorScale(val persisted: String, val menuLabel: String) {
    STOPS("CineStop", "CineStop 档位"),
    IRE("IRE", "IRE 电平"),
    LIMITS("限值", "限值"),
    EL_ZONE("EL Zone", "EL Zone 曝光"),
    ;

    companion object {
        fun fromPersisted(raw: String): FalseColorScale =
            entries.firstOrNull {
                it.persisted == raw || it.menuLabel == raw || it.name == raw
                    || raw == "Stops" || raw == "档" || raw == "ZC 档位" || raw == "PStops"
                    || raw == "Limits"
            } ?: STOPS

        fun fromMenuLabel(label: String): FalseColorScale =
            when (label) {
                "IRE", "IRE 电平" -> IRE
                "限值", "Limits" -> LIMITS
                "EL Zone", "EL Zone 曝光" -> EL_ZONE
                else -> STOPS
            }
    }
}

enum class ZebraUnit(val persisted: String, val editorLabel: String) {
    NATIVE("Native", "0-255"),
    IRE("IRE", "IRE"),
    ;

    companion object {
        fun fromPersisted(raw: String): ZebraUnit =
            entries.firstOrNull { it.persisted == raw || it.editorLabel == raw || it.name == raw }
                ?: IRE

        fun fromEditorLabel(label: String): ZebraUnit = if (label == "0-255") NATIVE else IRE
    }
}

enum class ZebraPaint(val label: String) {
    WHITE("白色"),
    AMBER("琥珀色"),
    RED("红色"),
    CYAN("青色"),
    GREEN("绿色"),
    ;

    /** Overlay RGB iOS `ZebraPaint.rgb` paints on the feed. */
    val rgb: Triple<Double, Double, Double>
        get() =
            when (this) {
                WHITE -> Triple(1.0, 1.0, 1.0)
                AMBER -> Triple(1.0, 0.72, 0.2)
                RED -> Triple(1.0, 0.15, 0.15)
                CYAN -> Triple(0.0, 0.85, 0.9)
                GREEN -> Triple(0.2, 0.9, 0.35)
            }

    companion object {
        fun fromPersisted(raw: String): ZebraPaint =
            entries.firstOrNull { it.label == raw || it.name == raw } ?: WHITE
    }
}

enum class WaveformMode(val label: String) {
    LUMA("亮度"),
    RGB("RGB"),
    ;

    companion object {
        fun fromPersisted(raw: String): WaveformMode =
            entries.firstOrNull { it.label == raw || it.name == raw } ?: RGB
    }
}

enum class ParadeMode(val label: String) {
    RGB("RGB"),
    YRGB("YRGB"),
    ;

    val laneCount: Int
        get() = if (this == YRGB) 4 else 3

    val laneLabels: List<String>
        get() = if (this == YRGB) listOf("Y", "R", "G", "B") else listOf("R", "G", "B")

    companion object {
        fun fromPersisted(raw: String): ParadeMode =
            entries.firstOrNull { it.label == raw || it.name == raw } ?: RGB
    }
}

enum class VectorscopeZoom(val label: String, val gain: Double) {
    X1("1x", 1.0),
    X2("2x", 2.0),
    X4("4x", 4.0),
    ;

    companion object {
        fun fromPersisted(raw: String): VectorscopeZoom =
            entries.firstOrNull { it.label == raw || it.name == raw } ?: X1
    }
}

/** OpenZCine `AssistConfiguration.CrushClipCompensation` — shared HISTO + LIGHTS. */
enum class CrushClipCompensation(val raw: Int, val label: String, val compactLabel: String) {
    ZERO(0, "0", "0"),
    QUARTER(2, "0.25", "¼"),
    HALF(5, "0.5", "½"),
    THREE_QUARTER(7, "0.75", "¾"),
    ONE(10, "1.0", "1"),
    ;

    val stops: Double
        get() =
            when (this) {
                ZERO -> 0.0
                QUARTER -> 0.25
                HALF -> 0.5
                THREE_QUARTER -> 0.75
                ONE -> 1.0
            }

    val pixelFractionThreshold: Double
        get() = stops / 10.0

    companion object {
        fun fromRaw(value: Int): CrushClipCompensation =
            entries.firstOrNull { it.raw == value } ?: if (value > 10) ONE else ZERO
    }
}

object LiveZebra {
    const val HIGHLIGHT_IRE = 99.0
    const val MIDTONE_IRE = 55.0
    const val MIDTONE_HALF_WIDTH_IRE = 5.0
}
