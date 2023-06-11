# Minecraft Dungeons Editor

A tool for modifying the in-game save data of Minecraft Dungeons for research purposes.  
Currently, MinecraftDungeons is only officially supported on Windows, but there are some running on Linux (Steam), so this tool supports Windows and Linux.

All images and text displayed within the tool are read from the installed game data, so it will not work on devices that do not have Minecraft Dungeons installed.

![Preview image](./preview.png)
(Above screenshot shows tool's language with Korean, but tool also supports English as default!)

## Comment
This is the version that just goes around.  
I didn't do any optimization or anything, and error handling is minimal, just if 'used in developer's purpose'  
I'll try to optimize reconfiguration and clean up the state and state holders and do some additional error handling soon.

Issues welcome!!!

## Download and install
Download the files for your operating system from the Release section.
- Linux: DungeonsEditor.AppImage  
  This is an executable file, so you can run it directly.
- Windows: DungeonsEditor.zip  
  Once unzipped to an appropriate location, you can run it through a file called DungeonsEditor.exe.

## Delete completely
- Linux: Delete the AppImage file, and delete the `~/.dungeons-editor` directory.
- Windows: Delete the directory you unzipped, and delete `%HOMEDRIVE%%HOMEPATH$/.dungeons-editor`. (This is usually `C:\Users\{{username}}\.dungeons-editor`.)

## Enable
### Open the file
1. run the tool and wait for it to finish looking for resource files.
    - If you see a screen that says 'Cannot find game resources', this means that you will need to specify your own game installation location.
2. Press the gray arrow on the right to manually select the files you want to edit, or select one of the files automatically found in the 'Detected Files' area on the bottom left.
    - Manually selecting a file  
      You'll see a file navigation screen. The UX is not friendly, so we recommend copying and pasting the path through another system explorer.  
      Press OK to select and enter the editor.
    - Selecting from Detected Files  
      The gray arrow on the right shows preview information. Confirm that this is the file you want to modify, and if so, press the gray arrow to enter the editor.

### Modify equipment
Select the equipment you want to modify from the list on the left. The selected equipment will be displayed on the right.
- Modifying Enchantments
    - Click on the enchantment you want to modify to open the detail screen.
    - You can change the applied effect to another effect in the left area of the detail screen.
    - In the left area of the detail screen, click an already applied enchantment again to change its slot as a locked slot.
    - In the right area of the detail screen, you can change the level of an enchantment.  
      Resetting an enchantment's level to 0 returns the slot to 'not yet active slot' (with three effects visible in one large slot). Use this method if you want to modify another effect that was in the slot without deleting an effect that is already active.
- Power  
  Modify by typing a value directly into the input box with labeled 'POWER'.
  However, each keystroke is validated and will be replaced with a typable real number, so it can be difficult to type the desired value directly(just copy-paste it).
- Type  
  To change a specific item to a different one, press "Replace" in the right of power edit field.  
  You can only change within the same item category (Melee, Armor, Ranged, Relic).
- Gilded Enchantment  
  Click on the locked slot with a gray background (or the 'GILDED' labeled enchantment with a yellow background) in the upper left corner of the item detail area (to the right of the rarity indicator) to modify it.  
  The basic changes are the same as for enchantments, with the following differences
    - Gilded enchantments cannot be set to a level of 0.
    - To remove a Gilded enchantment, similarly, tap the currently set enchantment again in the left area of the detail screen.
- Armor Attributes  
  The attributes that go on your armor can be manually specified.
    - To add a new armor attribute, click the + icon on the right, or to change an existing armor attribute, click the attribute you want to change.
    - To delete an existing attribute, click on the attribute you wish to delete firstly, then click on the selected property again on the left side of the detail screen.
- Custom
  Modify data related Enchantment resetting with merchant.
    - Click the "Modified" text to clear the "Custom" indicator in-game.
    - Click the number next to 'Modified' will allow you to modify the number of cost increases that will occur when the enchantment is reset in-game.

### Adding Equipment
Select the + icon in the bottom left corner. The equipment selection screen will appear.  
First select the type of equipment in the top left, then select the equipment you want to add from the list in the center.

All information can be edited after the item is added, so if you chose Equipment first, enter only the power and press OK.

The item is added. Tool will automatically select the added equipment, just start editing!

### Modifying Goods
In the bottom bar, Level, Emeralds, and Gold are the values that can be modified. Click and modify them.  
As you respectively know, the remaining enchantment points are calculated from player's level, so if you increase your level, your enchantment points will increase as well.

However, depending on the settings of your instrument, the enchantment point may drop to a negative number, in which case the effect point will be colored red.  
In this case, you should either level up more or lower the enchantment level on some of the enchanted items.

### Save
Save via the right "Chest" icon in the bottom bar.  
Again, the UX is not friendly, so I recommend copying and pasting the path from somewhere else.

- If you enter the path of a directory, it will save it in that directory with the same name as the original file.
- If you enter the path of a file, it will create the file with given name of path or overwrite it if it already exists.

It will automatically create a backup file in the directory where the file is saved, so you'll need to manually delete it if you think it's not needed.

## settings.arctic file
This is the set of settings that will be applied when you rerun the tool. You can set them by modifying the file named `settings.arctic` in the same location as the executable (AppImage, exe).  
There are currently two fields that can be set
- scale: Float  
  Sets the global scale of the tool. It is set to 0.5 by default and can be set within the range [0.4, 1.35].
- preload_textures: Boolean  
  Sets whether to preload texture files when startup of the tool. Sets whether to wait a bit at the startup and use them smoothly during execution (true), or to execute them quickly and accept some stuttering during tool execution (false).

An example configuration file looks like:
```
scale=0.5
preload_textures=true
```

## Building
It probably won't build because of the dependency on PakReader and the lack of a Keyset.
- Repository link of PakReader will be added here soon.
- For the Keyset, you'll need two AES keys, one to decrypt the Pak and one to decrypt the save data.  
  Please enter these keys in `/src/jvmMain/kotlin/Keyset.kt` like below.

```kotlin
class Keyset {
    companion object {
        val PakKey = listOf(
            // Key bytes here, with 32 bytes size
        )
            .map { it.toByte() }
            .toByteArray()
        val StoreKey = listOf(
            // Key bytes here, with 32 bytes size
        )
            .map { it.toByte() }
            .toByteArray()
    }
}
```

## Other
This program is not affiliated with a game developer and was developed for research purposes. Maybe...  
The program does not contain any in-game resources directly; it looks for and uses game installers in the environment in which it runs.
