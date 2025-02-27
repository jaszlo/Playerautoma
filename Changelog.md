# Version v0.5.2
* Bugfix:
  * Storing or having a recording file with some special characters or capital letters no longer crashes the game when opening the load recording screen
* Changes:
  * Now only a-z ".-_" are allowed to use when creating a recording file via the designated screen
  * Internal optimization for the JSON parsing
* New Features:
  * The TextField for the store record screen now only allows valid characters and invalid characters display an info message like the action bar 
  * Villager trading via the buttons to select a trade now gets recorded

# Version v0.5.1
* Bugfix:
  * Hotfixes Keybinding for Menuprevention not working

# Version v0.5.0
* New Features:
  * Quickmenu via (by default) holding F4 Key allows for
    * Easy Control of the PlayerRecorder (Start/Stop/Pause/Loop Replay/Recording)
    * Looping for n-times via left-clicking loop button n-times (or holding it down)
    * Looping indefinitely via right-clicking loop button once
    * Quickslot storing/loading/deletion via right-, left- or wheel-clicking  
  * Pausing the Recording is now possible via Quickmenu or Hotkey (Same hotkey as Pausing Replay by default)
  * `/record quickslot clear <1..9>` and `/record quickslot clear` commands have been added to clear single or all quickslot
  * Thumbnails as a preview for recordings are created and displayed for
    * storing screen
    * loading screen
    * quickmenu quickslots
  * Thumbnails are serialized to files and will be kept when sending a recording to a friend
  * Option to disable quickslot usage via `ctrl/alt + [1..9]` keybinds
  * Option to allow slot changes when using quickslots e.g. Ctrl + 1 will select slot 1. Was previously prevented
  * Option to disable quickslots from QuickMenu
  * Quickslots are now persistent and will be kept even after minecraft is closed
* Changes:
  * Info no longer written to chat rather appears in the players action bar
  * Drops spanish translation
  * Updates PlayerRecorder state icons
  * Many more descriptive error message (to action bar)
  * Moves all playerautoma related files into one `playerautoma` folder in the `.minecraft` folder
* Bugfix:
  * Playerautoma Keybindings will no longer be recorded
  * Now opening playerautoma main menu during replay is possible
  * Adds missing translations to german
  * `/record quickslot load|store` now work as intended
  

# Version v0.4.6
* New Features:
  * Non-Communication commands are now recorded and can be replayed
  * Specific or all commands can also be ignored via a toggle and blacklist option
  * New Option screen fo manage blacklist of commands
* Changes:
  * Recordings of older versions won't work with this version!

# Version 0.4.5:
* Bug Fixes:
  * Pressing the Menu Prevention Key while in chat no longer toggles menu prevention
* New Features:
  * Adds better support for [mod-menu](https://modrinth.com/mod/modmenu)
    * Accurate links
    * more meta-data
    * options screen reachable from mod-menu main screen

# Version 0.4.4:
* Bug Fixes:
  * Stopping Replay no longer says "Stopped recording"
  * Selection store as no longer determines how to load recording files
  * No longer overwrites existing file when _new already in name
  * Special Keys (e.g. dropping, swapping hands etc.) now work fine in replay
    * Still some minor error when holding button pressed
    * e.g. dropping one item short when holding 'q' to drop a stack
  * Modifiers such as CTRL + 'q' to drop a stack now work by tracking them for recording
* New Features:
  * Menu prevention allows to put minecraft in the background while replaying
  * Option to automatically enable menu prevention when starting a record
  * Option to reset all KeyBindings at start of recording for more consistency
  * New commands to store/load records to disk
    * Name suggestion for load command for existing files
  * Refresh Button for recording selector screen
* Removed Features:
  * Removes support for hindi and russian
* Changes
  * Commands now run only client side
  * Removes dot at the end of tooltips
  * Recording from previous versions are **NOT compatible**
  * Optimize .json/.rec files to reduce size

# Version 0.4.3:
* Bug Fixes:
  * Quickslot messages are now translatable text and no longer hard coded english strings
  * Invalid recording.json no longer crashes game
* New Features:
  * Adds commands to control the mod
    * /record <start|stop|clear>
    * /record quickslot <store|load> \<slot>
    * /replay <start|stop|loop|togglepause>

# Version 0.4.2:
* Bug Fixes:
  * Recorder now resets when leaving the game
  * Starting Replay from Pause no longer freezes Recorder
* New Features:
  * Json support for serialized Recordings
  * Icon to display state of Recorder (with options)
    * Show text
    * Show Icon
    * Show both
    * Show nothing

# Version 0.4.1:
* Bug Fixes:
  * Looped Replay works again
  * Delete Button works
* New Features:
  * Additional Options
    * Toggle chat output
    * Change HUD Position 

# Version 0.4.0:
* New Features:
  * Ability to pause and resume replays
  * Options Menu
    * Option to set/enable/disable default looking direction
    * Toggle Hud
    * Toggle inventory automation 
    * Toggle Relative/Absolute Looking direction when replaying
    * Toggle Recording of Inventory Activity
  * HUD Indicator which state player recorder is in
  * Language Support
    * German
    * Spanish
    * AI translation for hindi and russian 
* Bug Fixes:
  * Breaking a block in creative while replaying now works
  * Attacking entities while replaying now works
    * If while replaying the attack button is pressed and there is an entity in range, it will be attacked
* Changes:
  * Stop Replay Button removed from menu
# Version 0.3.3:
* Changes:
  * Logger no longer spams the console with player actions on replay
  * Selected Hotbar slot no longer changes when using Quickslots
  * Only allowing Quickslots from 1-9 resembling the Hotbar
  * More Feedback in the chat when loading from the Quickslots
  * No longer stores empty recordings to Quickslots
  * No longer loads empty recordings from Quickslots

# Version 0.3.2:
* New Features:
  * Quickslots can be used to store recordings via CTRL + 0-9 and load via ALT + 0-9

* Changes:
  * Inventory Screen no longer opens when re-stacking items

# Version 0.3.1:
* Hot Fix:
  * Looped Replay works again!

# Version 0.3:
* New Features:
    * Delete Button for Files
    * Slot-Clicking for Chest, Inventory, Crafting benches (allows for chest and crafting automation)

* Bug Fixes:
    * Empty Invalid Files were created on errors.
    * Files once opened could not be deleted due to unclosed resources
    * Inventory Screen not opening in replay
    * Selecting and deleting a file via file explorer would crash application on done

# Version 0.2:
* New Features:
    * Updates mod menu (cancel replay button added and text-update)
    * Store/Load mechanism

# Version 0.1:
* New Features:
    * Store/Load/Replay player movement and block placement
    * Re-Stacks items from the inventory like Inventory tweaks
