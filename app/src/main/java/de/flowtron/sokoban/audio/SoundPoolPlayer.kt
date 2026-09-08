package de.flowtron.sokoban.audio

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.util.Log
import de.flowtron.sokoban.R
import de.flowtron.sokoban.game.LevelParser
import de.flowtron.sokoban.game.LevelProgress
import de.flowtron.sokoban.state.StateFlowHolder
import de.flowtron.sokoban.ui.ToastHandler
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject

class SoundPoolPlayer @Inject constructor(
    private val stateFlowHolder: StateFlowHolder,
    private val toastHandler: ToastHandler,
) {

    private var mCSoundOne: Int = 0
    private lateinit var mSoundPool: SoundPool
    private var mLoaded = false

    var ready = false
    private set

    //companion object {}

    /* */
    fun initialise(applicationContext: Context) {
        Log.i("SoundPoolPlayer", "initialising..")
        mSoundPool = SoundPool(1, AudioManager.STREAM_MUSIC, 1)
        mSoundPool.setOnLoadCompleteListener(SoundPool.OnLoadCompleteListener { soundPool, sampleId, status ->
            Log.i("SoundPoolPlayer", "LOADED")
            mLoaded = true
            ready = true
            toastHandler.showToast("Enjoy The Game") // SoundPoolPlayer Is Ready
            //stateFlowHolder.
        })
        mCSoundOne = mSoundPool.load(applicationContext, R.raw.raphavpires__game_over, 1)
        Log.i("SoundPoolPlayer", "..initialised.")
    }
    /**/
    /*
    suspend fun ComponentTwo.initialiseAsync() = suspendCancellableCoroutine { continuation ->
    initialise(onComplete = { success ->
        if (continuation.isActive) continuation.resume(success)
    })
}
     */
    /*suspend fun initialiseAsync(context: Context) = suspendCancellableCoroutine { continuation ->
        initialise(context, onComplete = {
            if(continuation.isActive) continuation.resume()
        })

    }

    private fun initialise(context: Context, onComplete: () -> Unit) {
        Log.i("SoundPoolPlayer", "initialising..")
        mSoundPool = SoundPool(1, AudioManager.STREAM_MUSIC, 1)
        mSoundPool.setOnLoadCompleteListener(SoundPool.OnLoadCompleteListener { soundPool, sampleId, status ->
            Log.i("SoundPoolPlayer", "LOADED")
            mLoaded = true
            onComplete()
        })
        mCSoundOne = mSoundPool.load(context, R.raw.tak, 1)
        Log.i("SoundPoolPlayer", "..initialised.")
    }*/

    fun playOne() {
        if (mLoaded) {
            mSoundPool.play(mCSoundOne, 1.0f, 1.0f, 0, 0, 1.0f)
        }else{
            Log.d("SoundPoolPlayer", "Sound ONE not loaded.")
        }
    }
}