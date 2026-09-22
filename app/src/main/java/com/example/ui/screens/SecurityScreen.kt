package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@Composable
fun SecurityScreen(
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
            .testTag("security_screen")
    ) {
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "معايير الأمان والشفافية",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )
        Text(
            text = "كيف يعمل RAM Cleaner بمصداقية ودون أي خداع أو ادعاءات وهمية",
            fontSize = 12.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))

        SecurityPolicyCard(
            title = "لا أرقام وهمية أو مضللة",
            icon = Icons.Default.CheckCircle,
            accentColor = NeonEmerald,
            points = listOf(
                "قراءات RAM الموضحة تُستخرج مباشرة من واجهات ActivityManager.MemoryInfo الرسمية.",
                "لا ندّعي أبداً أن التطبيق يستطيع 'مضاعفة RAM' أو زيادة السعة الفيزيائية للجهاز.",
                "في حال لم يتم تحرير ذاكرة لأن النظام يديرها بكفاءة، يعرض التطبيق '0 MB' بصدق ووضوح تام."
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        SecurityPolicyCard(
            title = "بدون Root وبأمان تام 100%",
            icon = Icons.Default.Shield,
            accentColor = NeonCyan,
            points = listOf(
                "لا يطلب التطبيق أي صلاحيات Root أو Magisk أو تعديلات Kernel غير آمنة.",
                "نستخدم واجهة Android الرسمية: ActivityManager.killBackgroundProcesses().",
                "نظام أندرويد يقرر استعادة أو إبقاء الخدمات الحيوية وفق سياسات أمان نظام التشغيل."
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        SecurityPolicyCard(
            title = "حماية تطبيقات النظام والتطبيق النشط",
            icon = Icons.Default.Lock,
            accentColor = WarningAmber,
            points = listOf(
                "لا نقوم بقتل خدمات النظام الأساسية (System Core) أو واجهة الهاتف (System UI).",
                "لا يتم إغلاق التطبيق الذي تستخدمه أنت في تلك اللحظة (Foreground App).",
                "تجنب كامل لأوامر Force-Stop العشوائية التي قد تسبب فقدان العمل أو بطء إعادة الإقلاع."
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        SecurityPolicyCard(
            title = "كيف يدير Android الذاكرة؟",
            icon = Icons.Default.Info,
            accentColor = NeonCyan,
            points = listOf(
                "في أنظمة Linux و Android: 'الذاكرة الفارغة هي ذاكرة مهدورة'.",
                "يحتفظ أندرويد بصفحات التطبيقات في الـ Cached Memory لفتحها فورياً عند طلبها.",
                "يقوم الـ Low Memory Killer (LMK) بتحرير الذاكرة آلياً عند تشغيل لعبة أو تطبيق ثقيل."
            )
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SecurityPolicyCard(
    title: String,
    icon: ImageVector,
    accentColor: androidx.compose.ui.graphics.Color,
    points: List<String>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ObsidianSurface)
            .border(1.dp, ObsidianBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (point in points) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "• ",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                        Text(
                            text = point,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
