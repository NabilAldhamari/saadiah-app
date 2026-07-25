package app.saadiah.doctor

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

private fun component(
    packageName: String,
    className: String,
) = ComponentName(packageName, className)

private val AUTOSTART_SCREENS: Map<String, List<ComponentName>> =
    mapOf(
        "xiaomi" to
            listOf(
                component("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"),
            ),
        "huawei" to
            listOf(
                component(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity",
                ),
                component("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"),
            ),
        "oppo" to
            listOf(
                component("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"),
                component("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity"),
            ),
        "vivo" to
            listOf(
                component("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"),
                component("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"),
            ),
        "samsung" to
            listOf(
                component("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity"),
                component("com.samsung.android.sm", "com.samsung.android.sm.ui.battery.BatteryActivity"),
            ),
        "oneplus" to
            listOf(
                component("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"),
            ),
        "meizu" to
            listOf(
                component("com.meizu.safe", "com.meizu.safe.permission.SmartBGActivity"),
            ),
        "asus" to
            listOf(
                component("com.asus.mobilemanager", "com.asus.mobilemanager.autostart.AutoStartActivity"),
            ),
        "letv" to
            listOf(
                component("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity"),
            ),
    )

// Brands that ship another maker's firmware and inherit its restrictions.
private val BRAND_ALIASES =
    mapOf(
        "redmi" to "xiaomi",
        "poco" to "xiaomi",
        "honor" to "huawei",
        "realme" to "oppo",
    )

/**
 * Several manufacturers stop background apps by default, which silently kills prayer
 * alarms. These are the screens where a user can exempt this app. The class names move
 * between firmware versions, so a caller must check that the intent actually resolves.
 */
fun autostartComponentsFor(manufacturer: String): List<ComponentName> {
    val key = manufacturer.lowercase()
    return AUTOSTART_SCREENS[BRAND_ALIASES[key] ?: key].orEmpty()
}

fun isKnownRestrictive(manufacturer: String): Boolean = autostartComponentsFor(manufacturer).isNotEmpty()

/**
 * Ordered candidates, most specific first, ending with this app's own settings page,
 * which always exists. The caller launches them in turn until one succeeds; asking
 * whether each resolves would mean declaring package-visibility queries, and this app
 * has no business inspecting what else is installed.
 */
fun guidanceIntents(
    context: Context,
    manufacturer: String = Build.MANUFACTURER,
): List<Intent> =
    autostartComponentsFor(manufacturer).map { Intent().setComponent(it) } +
        Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS) +
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData(Uri.fromParts("package", context.packageName, null))
