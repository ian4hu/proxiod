package proxy.ian4hu.github.com

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlin.concurrent.timerTask

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).apply {
                setAction("Action") { _ -> dismiss()}
            }.show()

        }

        button_start.setOnClickListener { _ -> doJettyStart() }
        button_stop.setOnClickListener { _ -> doJettyStop() }
        button_status.setOnClickListener { _ -> doJettyStatus() }


        LocalBroadcastManager.getInstance(this).registerReceiver(object: BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                text_jetty_status.text = intent.getStringExtra("state");
            }

        }, IntentFilter(BROADCAST_STATUS))

    }

    override fun onStart() {
        super.onStart()

        AsyncTask.execute {
            doJettyStatus()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun doJettyStart() {
        JettyService.start(this, ACTION_START, Bundle.EMPTY);
    }

    fun doJettyStop() {
        JettyService.start(this, ACTION_STOP, Bundle.EMPTY);
    }

    fun doJettyStatus() {
        JettyService.start(this, ACTION_STATUS, Bundle.EMPTY)
    }
}
