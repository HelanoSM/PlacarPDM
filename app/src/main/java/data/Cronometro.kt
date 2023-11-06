package data

import android.os.Bundle
import java.util.Timer
import java.util.TimerTask

class Cronometro(private val tempoDuration: Int, private val speed: Double = 1.0) {
    var seconds = 0
        private set
    var tempoSeconds = 0
        private set
    var frozenSeconds = intArrayOf(0, 0)
        private set
    var isRunning = true
        private set
    var isFrozen = false
        private set
    var isFirstTime = true
        private set

    private val timer = Timer()
    private val events = HashMap<Int, () -> Unit>()
    private lateinit var onTick: () -> Unit

    fun start() {
        timer.schedule(object: TimerTask() {
            override fun run() {
                tick()
            }
        }, 0, (1000.0 / speed).toLong())
    }

    private fun tick() {
        if (isRunning) {
            seconds++
            if (!isFrozen) {
                tempoSeconds++
            } else {
                frozenSeconds[if (isFirstTime) 0 else 1] += 1
            }

            onTick()
            events[seconds]?.invoke()

            if (seconds >= tempoDuration + getAcrescimos()) {
                pause()
            }
        }
    }

    fun stop() {
        isRunning = false
        events[-1]?.invoke()
        timer.cancel()
    }
    fun kill() {
        timer.cancel()
        timer.purge()
    }
    fun freeze() {
        isFrozen = true
    }
    fun unfreeze() {
        isFrozen = false
    }
    fun pause() {
        val intervalo = if (isFirstTime) (15 * 60) else 0
        isRunning = false

        timer.schedule(object : TimerTask() {
            override fun run() {
                if (isFirstTime) {
                    isRunning = true
                    isFirstTime = false
                    tempoSeconds = 0
                    seconds = 0
                } else {
                    stop()
                }
            }
        }, ((intervalo*1000L)/speed).toLong())
    }

    fun getAcrescimos(): Int {
        val frozenSeconds = if (isFirstTime) frozenSeconds[0] else frozenSeconds[1]
        val acrescimos = frozenSeconds - (frozenSeconds % 60)

        return Math.min(acrescimos, 10 * 60)
    }

    fun setOnTick(onTick: () -> Unit) {
        this.onTick = onTick
    }

    fun addEvent(seconds: Int, event: () -> Unit) {
        events[seconds] = event
    }

    fun saveState(bundle: Bundle) {
        bundle.putInt("cronometro_seconds", seconds)
        bundle.putInt("cronometro_tempo_seconds", tempoSeconds)
        bundle.putIntArray("cronometro_frozen_seconds", frozenSeconds)
        bundle.putBoolean("cronometro_is_running", isRunning)
        bundle.putBoolean("cronometro_is_frozen", isFrozen)
        bundle.putBoolean("cronometro_is_first_time", isFirstTime)
    }

    fun restoreState(bundle: Bundle) {
        seconds = bundle.getInt("cronometro_seconds")
        tempoSeconds = bundle.getInt("cronometro_tempo_seconds")
        frozenSeconds = bundle.getIntArray("cronometro_frozen_seconds")!!
        isRunning = bundle.getBoolean("cronometro_is_running")
        isFrozen = bundle.getBoolean("cronometro_is_frozen")
        isFirstTime = bundle.getBoolean("cronometro_is_first_time")
    }
}
