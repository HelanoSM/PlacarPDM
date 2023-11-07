package ufc.smd.esqueleto_placar

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Button
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.getSystemService
import data.Cronometro
import data.Placar
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.charset.StandardCharsets

class PlacarActivity : AppCompatActivity() {
    lateinit var placar:Placar
    lateinit var tvResultadoJogo: TextView
    var game =0
    var golsTimeUm = 0
    var golsTimeDois = 0
    var ultimoPlacar = "$golsTimeUm X $golsTimeDois"
    val cronometro = Cronometro(300.0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_placar)
        placar= getIntent().getExtras()?.getSerializable("placar") as Placar
        tvResultadoJogo= findViewById(R.id.tvPlacar)
        tvResultadoJogo.text = ultimoPlacar


        val tvNomePartida=findViewById(R.id.tvNomePartida2) as TextView
        tvNomePartida.text=placar.nome_partida
        ultimoJogos()
        val time1 = intent.getStringExtra("time1")
        val time2 = intent.getStringExtra("time2")

        val nomeTimes1 = findViewById<Button>(R.id.nomestimes1)
        val nomeTimes2 = findViewById<Button>(R.id.nomestimes2)

        nomeTimes1.text = time1
        nomeTimes2.text = time2

        nomeTimes1.setOnClickListener {
            golsTimeUm++
            atualizaPlacar()
        }

        nomeTimes2.setOnClickListener {
            golsTimeDois++
            atualizaPlacar()
        }

        setupCronometro(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        cronometro.saveState(outState)
        cronometro.kill()
        super.onSaveInstanceState(outState)
    }

    private fun atualizaPlacar() {
        val placarFormatado = String.format("%d X %d", golsTimeUm, golsTimeDois)
        placar.resultado = placarFormatado
        ultimoPlacar = placarFormatado
        tvResultadoJogo.text = placar.resultado
    }

    fun alteraPlacar(v: View) {
        game++
        val placarFormatado = String.format("%d X %d", game, game - 1)
        placar.resultado = placarFormatado
        ultimoPlacar = placarFormatado
        tvResultadoJogo.text = ultimoPlacar
        vibrar(v)
    }

    fun vibrar (v:View){
        val buzzer = this.getSystemService<Vibrator>()
        val pattern = longArrayOf(0, 200, 100, 300)
        buzzer?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                buzzer.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                //deprecated in API 26
                buzzer.vibrate(pattern, -1)
            }
        }

    }


    fun saveGame(v: View) {

        val sharedFilename = "PreviousGames"
        val sp: SharedPreferences = getSharedPreferences(sharedFilename, Context.MODE_PRIVATE)
        var edShared = sp.edit()
        //Salvar o número de jogos já armazenados
        var numMatches= sp.getInt("numberMatch",0) + 1
        edShared.putInt("numberMatch", numMatches)

        //Escrita em Bytes de Um objeto Serializável
        var dt= ByteArrayOutputStream()
        var oos = ObjectOutputStream(dt);
        oos.writeObject(placar);

        //Salvar como "match"
        edShared.putString("match"+numMatches, dt.toString(StandardCharsets.ISO_8859_1.name()))
        edShared.commit() //Não esqueçam de comitar!!!

    }

    fun lerUltimosJogos(v: View) {
        val sharedFilename = "PreviousGames"
        val sp: SharedPreferences = getSharedPreferences(sharedFilename, Context.MODE_PRIVATE)

        val numMatches = sp.getInt("numberMatch", 0)
        if (numMatches > 0) {
            val lastMatchString = sp.getString("match$numMatches", "")
            if (lastMatchString != null) {
                if (lastMatchString.isNotEmpty()) {
                    val dis = ByteArrayInputStream(lastMatchString.toByteArray(Charsets.ISO_8859_1))
                    val oos = ObjectInputStream(dis)
                    val placarAntigo = oos.readObject() as Placar

                    tvResultadoJogo.text = "0 X 0"
                }
            }
        }
    }


    fun ultimoJogos () {
        val sharedFilename = "PreviousGames"
        val sp:SharedPreferences = getSharedPreferences(sharedFilename,Context.MODE_PRIVATE)
        var matchStr:String=sp.getString("match1","").toString()
        // Log.v("PDM22", matchStr)
        if (matchStr.length >=1){
            var dis = ByteArrayInputStream(matchStr.toByteArray(Charsets.ISO_8859_1))
            var oos = ObjectInputStream(dis)
            var prevPlacar:Placar = oos.readObject() as Placar
            Log.v("PDM22", "Jogo Salvo:"+ prevPlacar.resultado)
        }

    }

    fun setupCronometro(bundle: Bundle?) {
        val btnCronometro = findViewById<Button>(R.id.btnCronometro)
        bundle?.let {
            cronometro.restoreState(bundle)
            btnCronometro.text = if (cronometro.isFrozen) "Play" else "Pause"
        }

        btnCronometro.setOnClickListener {
            if (cronometro.isFrozen) {
                cronometro.unfreeze()
                btnCronometro.text = "Pause"
            } else {
                cronometro.freeze()
                btnCronometro.text = "Play"
            }
        }

        cronometro.setOnTick {
            runOnUiThread {
                Log.v("PDM22", "Tempo: " + cronometro.tempoSeconds)
                val segundos = cronometro.seconds % 60
                val minutos = cronometro.seconds / 60
                val duracaoText = minutos.toString().padStart(2, '0') +
                        ":" + segundos.toString().padStart(2, '0')
                val tvDuracao = findViewById<TextView>(R.id.tvCronometroDuracao)
                tvDuracao.text = duracaoText

                val tvAcrescimos = findViewById<TextView>(R.id.tvCronometroAcresimos)
                if (cronometro.seconds/60 >= 42) {
                    Log.v("PDM22", "Acrescimos: " + cronometro.getAcrescimos())
                    val acrescimos = "+${cronometro.getAcrescimos()/60}"
                    tvAcrescimos.text = acrescimos
                    tvAcrescimos.visibility = View.VISIBLE
                } else {
                    tvAcrescimos.visibility = View.INVISIBLE
                }

                val tempo = if (cronometro.isFirstTime) "1º" else "2º"
                val tvTempo = findViewById<TextView>(R.id.tvCronometroTempo)
                tvTempo.text = tempo
            }
        }

        cronometro.addEvent(-1) {
            Log.v("PDM22", "Fim da partida")
            // finalizar a partida
        }

        cronometro.start()
    }
}
