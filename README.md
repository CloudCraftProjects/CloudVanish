# CloudVanish

Minecraft paper plugin for vanishing staff members from players, supporting layered vanish permissions.

## Download

Download: https://dl.cloudcraftmc.de/cloudvanish

> [!NOTE]
> Depends
> on [CommandAPI](https://modrinth.com/project/commandapi) and
> [CloudCore](https://modrinth.com/project/cloudcore).

## Usage

### Commands

- Use `/v list` to list online vanished players
- Use `/v [<target>]` to toggle vanish mode for yourself or the specified target
- Use `/v pickup [<target>]` to toggle vanish item/exp pickup mode for yourself or the specified target

### Permissions

Aside from permissions assigned to commands, the following permissions additionally control the behaviour of CloudVanish:
- `cloudvanish.use`: decides wether the player is allowed to vanish at all
- `cloudvanish.level.{1,100}`: decides which vanish level the player is assigned to
  - Players with a vanish level less than 1 will not be able to vanish at all
  - Players with a higher vanish level will be able to see players with a lower vanish level
  - Example: Player A has vanish level 3, Player B has vanish level 4, Player C is a normal player. Player C won't see anyone, Player A will see Player C but not Player B. Player C will see everyone, but will be seen by no one.

## License

Licensed under GPL-3.0, see [LICENSE](./LICENSE) for further information.
