
package id.timelinewallpaper

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.toMutablePreferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionRunCallback
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.PreferencesGlanceStateDefinition
import androidx.glance.currentState
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.text.FontWeight
import androidx.glance.unit.ColorProvider
import androidx.glance.state.GlanceStateDefinition
import androidx.datastore.preferences.core.Preferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Black = Color(0xFF000000)
private val White = Color(0xFFF7F7F7)
private val Teal = Color(0xFF00A99F)
private val TealLight = Color(0xFF55E4DB)
private val Muted = Color(0xFF858585)

private val tasks = listOf(
    "Bangun pagi" to "Pukul 05.00",
    "Baca buku & rangkuman" to "Minimal 10 halaman",
    "Olahraga" to "Minimal 30 menit",
    "Public speaking" to "Belajar minimal 10 menit",
    "Bahasa Inggris" to "Belajar minimal 10 menit"
)

private fun taskKey(i: Int) = booleanPreferencesKey("task_$i")
private val dateKey = stringPreferencesKey("saved_date")
private val TaskIndexKey = ActionParameters.Key<Int>("task_index")

class TimelineWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*> =
        PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val now = Date()
            val day = SimpleDateFormat("EEEE", Locale("id", "ID"))
                .format(now).uppercase(Locale("id", "ID"))
            val date = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                .format(now).uppercase(Locale("id", "ID"))
            val time = SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(now)

            val prefs = currentState<Preferences>()
            val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(now)
            val flags = tasks.indices.map { i ->
                if (prefs[dateKey] == todayKey) prefs[taskKey(i)] ?: false
                else false
            }
            val done = flags.count { it }
            val pct = done * 100 / tasks.size

            Column(
                modifier = GlanceModifier.fillMaxSize()
                    .background(Black).padding(12.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(day, style = TextStyle(
                            color = ColorProvider(TealLight),
                            fontWeight = FontWeight.Bold
                        ))
                        Text(date, style = TextStyle(
                            color = ColorProvider(White),
                            fontWeight = FontWeight.Bold
                        ))
                    }
                    Text(time, style = TextStyle(
                        color = ColorProvider(White),
                        fontWeight = FontWeight.Bold
                    ))
                }

                Spacer(GlanceModifier.height(10.dp))
                Text("TIMELINE HARI INI", style = TextStyle(
                    color = ColorProvider(TealLight),
                    fontWeight = FontWeight.Bold
                ))
                Spacer(GlanceModifier.height(4.dp))

                tasks.forEachIndexed { i, task ->
                    Row(
                        modifier = GlanceModifier.fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (flags[i]) "✓" else "○",
                            modifier = GlanceModifier.width(28.dp)
                                .clickable(
                                    actionRunCallback<ToggleTaskAction>(
                                        actionParametersOf(TaskIndexKey to i)
                                    )
                                ),
                            style = TextStyle(
                                color = ColorProvider(if (flags[i]) TealLight else White),
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Column(modifier = GlanceModifier.defaultWeight()) {
                            Text(task.first, style = TextStyle(
                                color = ColorProvider(if (flags[i]) Muted else White),
                                fontWeight = FontWeight.Bold
                            ))
                            Text(task.second, style = TextStyle(
                                color = ColorProvider(Muted)
                            ))
                        }
                    }
                }

                Spacer(GlanceModifier.height(8.dp))
                Column(
                    modifier = GlanceModifier.fillMaxWidth()
                        .background(Color(0xFF101817)).padding(10.dp)
                ) {
                    Text("PROGRES HARI INI   $done/${tasks.size}",
                        style = TextStyle(
                            color = ColorProvider(TealLight),
                            fontWeight = FontWeight.Bold
                        ))
                    Spacer(GlanceModifier.height(4.dp))
                    Text("$pct%", style = TextStyle(
                        color = ColorProvider(White),
                        fontWeight = FontWeight.Bold
                    ))
                    Text(
                        if (pct == 100) "Semua selesai, mantap!"
                        else if (pct > 0) "Bagus, terus lanjut"
                        else "Ayo mulai harimu",
                        style = TextStyle(color = ColorProvider(TealLight))
                    )
                }
            }
        }
    }
}

class ToggleTaskAction : ActionCallback {
    override suspend fun onRun(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val i = parameters[TaskIndexKey] ?: return
        if (i !in tasks.indices) return

        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Date())

        updateAppWidgetState(
            context, PreferencesGlanceStateDefinition, glanceId
        ) { prefs ->
            val editor = prefs.toMutablePreferences()
            if (editor[dateKey] != todayKey) {
                tasks.indices.forEach { editor.remove(taskKey(it)) }
                editor[dateKey] = todayKey
            }
            editor[taskKey(i)] = !(editor[taskKey(i)] ?: false)
            editor
        }
        TimelineWidget().update(context, glanceId)
    }
}

class TimelineWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TimelineWidget()
}
