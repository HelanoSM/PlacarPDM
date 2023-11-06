package ufc.smd.esqueleto_placar

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Switch
import data.Placar

class ConfigActivity : AppCompatActivity() {
    var placar: Placar= Placar("Digite as Configurações do Jogo","0x0", "01/01/23 10h",false)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)
       // placar= getIntent().getExtras()?.getSerializable("placar") as Placar
        //Log.v("PDM22",placar.nome_partida)
        //Log.v("PDM22",placar.has_timer.toString())


        openConfig()
        initInterface()

    }
    fun saveConfig(){
        val sharedFilename = "configPlacar"
        val sp:SharedPreferences = getSharedPreferences(sharedFilename,Context.MODE_PRIVATE)
        var edShared = sp.edit()


        edShared.putString("matchname",placar.nome_partida)
        edShared.putBoolean("has_timer",placar.has_timer)
        edShared.commit()
    }
    fun openConfig()
    {
        val sharedFilename = "configPlacar"
        val sp:SharedPreferences = getSharedPreferences(sharedFilename,Context.MODE_PRIVATE)
        placar.nome_partida=sp.getString("matchname","Jogo Padrão").toString()
        placar.has_timer=sp.getBoolean("has_timer",false)

    }
    fun initInterface(){
        val tv= findViewById<EditText>(R.id.editTextGameName)
        tv.setText(placar.nome_partida)
        val sw= findViewById<Switch>(R.id.swTimer)
        sw.isChecked=placar.has_timer
    }

    fun updatePlacarConfig(){
        val tv= findViewById<EditText>(R.id.editTextGameName)
        val sw= findViewById<Switch>(R.id.swTimer)
        placar.nome_partida= tv.text.toString()
        placar.has_timer=sw.isChecked
    }

    fun openPlacar(v: View){
        updatePlacarConfig()
        saveConfig()
        val nomeTime1 = findViewById<EditText>(R.id.nometime1).text.toString()
        val nomeTime2 = findViewById<EditText>(R.id.nometime2).text.toString()
        val intent = Intent(this, PlacarActivity::class.java).apply{
            putExtra("placar", placar)
            putExtra("time1", nomeTime1)
            putExtra("time2", nomeTime2)
        }
        startActivity(intent)
    }
}