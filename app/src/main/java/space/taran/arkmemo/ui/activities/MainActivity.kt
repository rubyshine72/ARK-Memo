package space.taran.arkmemo.ui.activities

import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import space.taran.arkfilepicker.presentation.onArkPathPicked
import space.taran.arkmemo.R
import space.taran.arkmemo.contracts.PermissionContract
import space.taran.arkmemo.data.viewmodels.TextNotesViewModel
import space.taran.arkmemo.databinding.ActivityMainBinding
import space.taran.arkmemo.files.FilePicker
import space.taran.arkmemo.models.TextNote
import space.taran.arkmemo.preferences.MemoPreferences
import space.taran.arkmemo.time.MemoCalendar
import space.taran.arkmemo.ui.fragments.EditTextNotes
import space.taran.arkmemo.ui.fragments.Settings
import space.taran.arkmemo.ui.fragments.TextNotes

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val binding by viewBinding(ActivityMainBinding::bind)

    @IdRes
    private val fragContainer = R.id.container

    private val textNotesViewModel: TextNotesViewModel by viewModels()

    init {
        FilePicker.readPermLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) FilePicker.show()
                else finish()
            }

        FilePicker.readPermLauncher_SDK_R =
            registerForActivityResult(PermissionContract()) { isGranted ->
                if (isGranted) FilePicker.show()
                else finish()
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            setContentView(binding.root)
            setSupportActionBar(binding.toolbar)
            binding.toolbar.setNavigationOnClickListener {
                onBackPressed()
            }

            if (MemoPreferences.getInstance(this).getPath() == null)
                FilePicker.show(this, supportFragmentManager)

            val textDataFromIntent = intent?.getStringExtra(Intent.EXTRA_TEXT)
            if(textDataFromIntent != null){
                val editTextNotes = EditTextNotes(textDataFromIntent)
                editTextNotes.noteDate = MemoCalendar.getDateToday()
                editTextNotes.noteTimeStamp = MemoCalendar.getFullDateToday()
                supportFragmentManager.beginTransaction().apply{
                    replace(fragContainer, editTextNotes, EditTextNotes.TAG)
                    commit()
                }
            }
            else
            {
                supportFragmentManager.beginTransaction().apply {
                    add(fragContainer, TextNotes(), TextNotes.TAG)
                    commit()
                }
            }

            supportFragmentManager.onArkPathPicked(this) {
                MemoPreferences.getInstance(this).storePath(it.toString())
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        mMenu = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> {
                replaceFragment(Settings(), Settings.TAG)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
            }
        }
        return true
    }
}

private var mMenu: Menu? = null

fun AppCompatActivity.replaceFragment(fragment: Fragment, tag: String) {
    supportFragmentManager.beginTransaction().apply {
        val backStackName = fragment.javaClass.name
        val popBackStack = supportFragmentManager.popBackStackImmediate(backStackName, 0)
        if (!popBackStack) {
            replace(R.id.container, fragment, tag)
            addToBackStack(backStackName)
        } else {
            show(fragment)
        }
        commit()
    }
}

fun AppCompatActivity.deleteTextNote(note: TextNote){
    val textNotesViewModel: TextNotesViewModel by viewModels()
    textNotesViewModel.deleteTextNote(this, note)
}

fun Context.getTextFromClipBoard(): String?{
    val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
    return clipboardManager.primaryClip?.getItemAt(0)?.text?.toString()
}

fun hideSettingsButton(){
    val settingsItem = mMenu?.findItem(R.id.settings)
    settingsItem?.isVisible = false
}

fun showSettingsButton(){
    val settingsItem = mMenu?.findItem(R.id.settings)
    settingsItem?.isVisible = true
}
