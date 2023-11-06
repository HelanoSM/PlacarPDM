package ufc.smd.esqueleto_placar

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator

import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.getSystemService
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
    var ultimoPlacar: String = "0 X 0"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_placar)
        placar= getIntent().getExtras()?.getSerializable("placar") as Placar
        tvResultadoJogo= findViewById(R.id.tvPlacar)
        tvResultadoJogo.text = ultimoPlacar
        //Mudar o nome da partida
        val tvNomePartida=findViewById(R.id.tvNomePartida2) as TextView
        tvNomePartida.text=placar.nome_partida
        ultimoJogos()
        val time1 = intent.getStringExtra("time1")
        val time2 = intent.getStringExtra("time2")

        val nomeTimes1 = findViewById<TextView>(R.id.nomestimes1)
        val nomeTimes2 = findViewById<TextView>(R.id.nomestimes2)

        nomeTimes1.text = time1
        nomeTimes2.text = time2
    }

    fun alteraPlacar (v:View){
        game++
        if ((game % 2) != 0) {
            placar.resultado = ""+game+" vs "+ (game-1)
        }else{
            placar.resultado = ""+(game-1)+" vs "+ (game-1)
            vibrar(v)
        }
        ultimoPlacar = placar.resultado
        tvResultadoJogo.text=placar.resultado
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

                    tvResultadoJogo.text = placarAntigo.resultado
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
}