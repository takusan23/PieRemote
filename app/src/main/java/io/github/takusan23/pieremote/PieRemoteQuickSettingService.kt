package io.github.takusan23.pieremote

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/** クイック設定タイルを作るやつ */
@RequiresApi(Build.VERSION_CODES.N)
class PieRemoteQuickSettingService : TileService() {

    private var scope: CoroutineScope? = null

    override fun onStartListening() {
        super.onStartListening()
        scope = MainScope()
        scope?.launch {
            dataStore.data.collect {
                updateTile(isServiceEnable = it[DataStore.IS_SERVICE_ENABLE] == true)
            }
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        scope?.cancel()
        scope = null
    }

    override fun onClick() {
        super.onClick()
        scope?.launch {
            dataStore.edit {
                it[DataStore.IS_SERVICE_ENABLE] = !(it[DataStore.IS_SERVICE_ENABLE] == true)
            }
        }
    }

    private fun updateTile(isServiceEnable: Boolean) {
        if (isServiceEnable) {
            qsTile.label = getString(R.string.quick_setting_on_label)
            qsTile.state = Tile.STATE_ACTIVE
        } else {
            qsTile.label = getString(R.string.quick_setting_off_label)
            qsTile.state = Tile.STATE_INACTIVE
        }
        qsTile.updateTile()
    }

}