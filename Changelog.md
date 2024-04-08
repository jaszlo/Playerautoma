# Version 0.4.4:
* Bug Fixes:
  * Stopping Replay no longer says "Stopped recording"
  * Selection store as no longer determines how to load recording files
  * No longer overwrites existing file when _new already in name
  * Special Keys (e.g. dropping, swapping hands etc.) now work fine in replay
* New Features:
  * Menu prevention allows to put minecraft in the background while replaying
  * Option to automatically to enable menu prevention when starting a record
  * Commands now run on client side
  * New commands to store/load records to disk
    * Name suggestion for load command for existing files
  * Refresh Button for recording selector screen
* Removed Features:
  * Removes support for hindi and russian

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
