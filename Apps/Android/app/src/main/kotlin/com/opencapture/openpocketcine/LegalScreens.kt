package com.opencapture.openpocketcine

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class LegalKind(val title: String) {
    PRIVACY("隐私"),
    TERMS("条款"),
    LICENSES("许可证"),
    NOTICE("声明"),
    ;

    val body: String
        get() =
            when (this) {
                PRIVACY -> PRIVACY_BODY
                TERMS -> TERMS_BODY
                LICENSES -> LICENSES_BODY
                NOTICE -> NOTICE_BODY
            }
}

@Composable
fun LegalDocumentScreen(
    kind: LegalKind,
    onClose: () -> Unit,
) {
    BackHandler(onBack = onClose)
    val shape = RoundedCornerShape(LiveDesign.CORNER_RADIUS_DP.dp)
    Box(
        Modifier
            .fillMaxSize()
            .background(LiveDesign.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 12.dp),
        ) {
            Column(Modifier.padding(start = 45.dp)) {
                Text(
                    "OPENPOCKETCINE",
                    style = LiveType.ui(9.5f, FontWeight.Bold).copy(letterSpacing = 0.8.sp),
                    color = LiveDesign.accent,
                )
                Text(
                    kind.title,
                    style = LiveType.title(24f, FontWeight.SemiBold),
                    color = LiveDesign.text,
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                kind.body,
                style = LiveType.ui(14f, FontWeight.Normal).copy(lineHeight = 19.sp),
                color = LiveDesign.text,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .background(LiveDesign.surface, shape)
                        .border(1.dp, LiveDesign.hairline, shape)
                        .padding(16.dp),
            )
        }
        OperatorCloseButton(
            onClose = onClose,
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp, top = 16.dp),
        )
    }
}

private val PRIVACY_BODY =
    """
OpenPocketCine 通过蓝牙和相机自身的 Wi-Fi 与你的 Osmo Pocket 通信。它不会创建账号，也不会向我们发送分析数据、崩溃报告或相机素材。

保留在这台手机上的内容
• 已保存的相机名称和上次的 SSID。相机 Wi-Fi 密码只保存在本手机的 Android Keystore 中。
• 操作员偏好设置（如保持屏幕常亮）和上一次使用的 LUT。
• 你选择打开的 .cube 导入文件。

永远不会离开手机的内容
• 实时 HEVC 流、DUML 遥测和配对流量都只走本地链路。我们不运营任何云端服务。

第三方
• Apple（或 Google）可能按其自身政策处理权限弹窗和系统诊断。TestFlight 或 Play 测试版可能按 Apple/Google 条款向开发者发送崩溃报告。
• Frame.io 为可选功能。若已配置并登录，你选择的片段会从手机上传到 Adobe。令牌只保存在设备的 Keystore 中。
• 保存片段或使用系统分享由你主动发起，之后由 Apple 或你选择的应用处理该文件。
• AF-C 人脸框在本手机上根据实时预览计算，人脸几何信息不会上传。
• 源代码在 github.com/erik-sutton95/OpenPocketCine。

Android 可能请求位置权限，用于加入相机 Wi-Fi 或扫描 BLE。OpenPocketCine 不会将该权限用于地图、广告或位置历史。

本文不构成法律意见。正式的网站政策见 https://openpocketcine.app/privacy/
    """.trimIndent()

private val TERMS_BODY =
    """
OpenPocketCine 是基于 Apache License 2.0 的自由软件。你可以在该许可证下使用、修改和分发它。

这是一个非官方的监视器应用，与 DJI 没有关联，也未获得 DJI 的认可或支持。"DJI"、"Osmo"、"Osmo Pocket" 是深圳市大疆创新科技有限公司的商标，此处仅用于标识本应用可连接的相机型号。

逆向工程的协议行为可能不完整或有误。在相关指令得到验证之前，请不要把本应用作为开始/停止录制的唯一手段——请同时在机身上确认录制状态。

本软件按"原样"提供，不附带任何形式的保证或条件。许可证全文见仓库中的 LICENSE。

使用本应用即表示你同意在参与项目时遵守行为准则（Code of Conduct）。
    """.trimIndent()

private val LICENSES_BODY =
    """
OpenPocketCine
Copyright 2026 Erik Sutton and OpenPocketCine contributors

基于 Apache License 2.0 授权。许可证全文见：

http://www.apache.org/licenses/LICENSE-2.0

可移植的 .cube 解析器（CubeLUT）改编自 OpenZCine（同为 Apache 2.0）。

HUD 图标来自 Lucide（ISC 许可证；部分字形来自 Feather，MIT 许可证）。见 THIRD-PARTY-NOTICES。

蓝牙配对与相机 Wi-Fi 连接方案的实现得到了 Konrad Iturbe 的 Osmosis 项目帮助，在此致谢。OpenPocketCine 是独立实现；监视器架构参照 OpenZCine。

本应用不包含也不需要 DJI SDK。

许可证全文：仓库中的 LICENSE。署名信息：NOTICE。
    """.trimIndent()

private val NOTICE_BODY =
    """
OpenPocketCine
Copyright 2026 Erik Sutton and OpenPocketCine contributors

本产品基于 Apache License 2.0 授权（见 LICENSE）。
可移植的 .cube 解析器（`CubeLUT`）改编自 OpenZCine（Apache 2.0）。
HUD 图标来自 Lucide（ISC 许可证；源自 Feather 的字形同为 MIT）。

本项目与深圳市大疆创新科技有限公司没有关联，也未获得其认可。
"DJI"、"Osmo"、"Osmo Pocket"、"Mimo" 是深圳市大疆创新科技有限公司的商标，
此处仅用于标识。
本项目中不包含、不分发也不需要 DJI SDK 或 DJI 专有文档。
    """.trimIndent()
