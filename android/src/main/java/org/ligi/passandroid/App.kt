package org.ligi.passandroid

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.github.salomonbrys.kodein.*
import com.jakewharton.threetenabp.AndroidThreeTen
import com.squareup.moshi.Moshi
import org.greenrobot.eventbus.EventBus
import org.ligi.passandroid.json_adapter.ColorAdapter
import org.ligi.passandroid.json_adapter.ZonedTimeAdapter
import org.ligi.passandroid.model.AndroidFileSystemPassStore
import org.ligi.passandroid.model.AndroidSettings
import org.ligi.passandroid.model.PassStore
import org.ligi.passandroid.model.Settings
import org.ligi.tracedroid.TraceDroid
import org.ligi.tracedroid.logging.Log

open class App : Application() {

    override fun onCreate() {
        super.onCreate()

        kodein = Kodein {
            import(createTrackerKodeinModule(this@App))
            import(createKodein(), allowOverride = true)
        }

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        AndroidThreeTen.init(this)
        initTraceDroid()

        val settings: Settings = kodein.instance()
        AppCompatDelegate.setDefaultNightMode(settings.getNightMode())
    }

    open fun createKodein() = Kodein.Module {
        val build = Moshi.Builder()
                .add(ZonedTimeAdapter())
                .add(ColorAdapter())
                .build()


        bind<PassStore>() with singleton { AndroidFileSystemPassStore(this@App, instance(), build, instance()) }
        bind<Settings>() with singleton { AndroidSettings(this@App) }
        bind<EventBus>() with singleton { EventBus.getDefault() }
    }

    private fun initTraceDroid() {
        TraceDroid.init(this)
        Log.setTAG("PassAndroid")
    }

    companion object {
        lateinit var kodein: Kodein
        val tracker by lazy { kodein.Instance(TT(Tracker::class.java)) }
        val passStore by lazy { kodein.Instance(TT(PassStore::class.java)) }
        val settings by lazy { kodein.Instance(TT(Settings::class.java)) }
    }
}
