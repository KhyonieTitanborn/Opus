# Titanscript
*A fast and flexible way for anyone to create functional quests using Opus.*

*"/!\\" indicates an instruction is not implemented yet, beware.* 

### Variables
Variables can be set by name in any variable-setting instruction, and can be referenced in any instruction by a dollar $ sign and the name. 
Three variables can always be accessed: the player's coordinates, as $playerX, $playerY, and $playerZ

At this time, only integers and entities can be stored.

Any time an instruction expects a variable instead of a number, the parameter will read "`<var:type>`" where type is a datatype.

### Datatypes
`int`: A number with no fractional component.

`double/float`: A number with a fractional component.

`string`: A piece of text. Strings can be concatenated by encasing the entire string in quotes. Single-word strings do not require quotes.

`entity`: A spawned entity in a world.

`world`: A Minecraft world. This is the world's canonical name (I.E what's saved when the server shuts down, often "world", "world_nether", and "world_the_end").

Some instructions may require a canonical Java enum to be supplied, this will usually look like `<org.bukkit.entity.EntityType>`. When these enums are required, the necessary documentation will be supplied below the instruction, and possible values will be under "Enum Constant Summary" on these pages, and will be in all uppercase.

When it may not be entirely clear what a parameter is, the parameter will be shown as "`<name (type)>`".

## Logic instructions

`Jump <int>`
- Moves execution to the specified line.

`JumpTrue <int>`
- Moves execution to the specified line if and only if the output of the previous boolean instruction was true.

`JumpNot <int>`
- Moves execution to the specified line if and only if the output of the previous boolean instruction was false.

`JumpEqual <int> <int>`
- Moves execution to the specified line if and only if the two given integers are equal.

`JumpNotEqual <int> <int>`
- Moves execution to the specified line if and only if the two given integers are not equal.

`Call <int>`
- Sets $return to the next line and moves execution to the specified line.
- Example:
```
Print "This is line 1"
Call 5
// $return is now 4 (comments are counted as lines)
EndScript
Print "This is line 4"
Return
// ^ Jumps to line 3
Print "This will never be executed!"
```

`Return`
- Moves execution to the line $return.

`Delay <int>`
- Delays the next instruction until after the specified number of ticks have elapsed.
- After this instruction, the `Synchronize` instruction may be required.

`Synchronize`
- Synchronizes the next instruction to happen on the next tick. If any instruction desynchronizes from a tick, most instructions after will fail.

## Variable instructions

`SetInteger <string> <int>`
- Sets an integer variable with the given name and value.

`SetReturn <int>`
- Sets $return to the specified value.

## Arithmetic instructions

`Increment <var:int>`
- Increments the given integer variable by 1.

`Decrement <var:int>`
- Decrements the given integer variable by 1.


## Boolean instructions 
*After execution, boolean instructions can be compared with logic instructions to branch execution.*

/!\ `HasCompleted <string>`
- Checks whether or not a player has completed an objective, looked up by name.

`Compare <int> <int>`
- Checks whether or not the two given integers are equal.


## Script instructions
`EndScript`
- Ends script execution.

`Print <*>`
- Prints the given string into the server console.
*Note: Quotes can be used to concatenate a string, however with variables, variables must be seperate from any quotation marks. *
Example: Correct: `Print " $aVar is the count, the count is $aVar "`, Incorrect: "$aVar is the count, the count is $aVar"


## World instructions

`SetWorld <string>`
- Sets the specified world as the target. Name should match a world file on disk, in most cases, this should be "world" for the overworld, "world_nether" for the nether, and "world_the_end" for the end.
This is required for most world instructions to work, only one call is necessary per script.

`PlayWorldSound <x (double)> <y (double)> <z (double)> <org.bukkit.Sound> <volume (float)> <pitch (float)>`
- Plays a sound audible to all players within range, centered on the given (X, Y, Z) location, with the specified pitch and volume (Usually 1.0 for both).
- Uses Java org.bukkit.Sound enum, see https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html
- *Requires world to be set.*

`BreakBlock <x (double)> <y (double)> <z (double)>`
- Breaks a block at the specified location.
- *Requires world to be set.*

`SpawnFallingBlock <x (int)> <y (int)> <z (int)>`
- Spawns a falling block matching the block already existing at the given coordinates.
- *Requires world to be set.*

`SpawnRegularEntity <string> <org.bukkit.entity.EntityType> <x (double)> <y (double)> <z (double)>`
- Spawns an entity at the given coordinates.
- Uses Java org.bukkit.entity.EntityType, see https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html
- Exact entity can be targetted with entity instructions by referring to the given string.
- *Requires world to be set.*


## Entity instructions

`GiveEffect <entity> <effect> <duration in ticks (int)> <amplifier>`
- Applies a potion effect to an entity.
- If "player" is the specified target, the player executing this script will be targetted. Otherwise an entity's storage name must be used.
*Minecraft runs at 20 ticks per second. 1 second = 20 ticks, 1 minute = 1200 ticks*

`GiveItem <entity> <org.bukkit.inventory.EquipmentSlot> <org.bukkit.Material>`
- Sets an item to an entity's equipment slot.
- Uses Java org.bukkit.inventory.EquipmentSlot, see https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/EquipmentSlot.html
- Uses Java org.bukkit.Material, see https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html

`SetEntityName <entity> <string>`
- Sets an entity's display name (the name that appears above their head).


## Player instructions

`Chat <string>`
- Sends the given message to the player executing this script.

`PlaySound <org.bukkit.Sound> <volume (float)> <pitch (float)>`
- Plays a per-player sound (not audible to other players) at the player's location.
- Uses Java org.bukkit.Sound, see https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html

`PrimeScriptButton`
- Allows a player to execute a script-altering command, such as `/titanscript jump 5`, which should always be done through a chat button.

`SendChatButton <string>`
- Sends the given message to the player executing this script, with support for command-executing buttons.
- All titanscript commands will require the `PrimeScriptButton` instruction to be used.
- Buttons are inserted in the following format: "<Button text:command without slash>".
- Ex: 
```
PrimeScriptButton // Prime titanscript action
SendChatButton "Click here to jump to line 5: <Click me!:titanscript jump 5>"
```

## Recipe book
*A collection of little snippets.*

### Loop 
- A simple loop that counts from 0 to 9, printing the current loop count
```
SetInteger loopCounter 0
Print " $loopCounter "
Increment loopCounter
JumpNotEqual $loopCounter 10 2
Print "Loop finished."
```
