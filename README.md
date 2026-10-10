# Saddle

Plugin suite for the unofficial PayMoneyWubby server

## Map

simple /map command linking to a webmap. Link configurable

## Rules

Forces players to read a configurable rules file on first login

### commands

| Command        | Description                                               | permissions    |
|----------------|-----------------------------------------------------------|----------------|
| `/rules`       | Opens the rules GUI                                       | everyone       |
| `/rules reload | Reloads the rules config                                  | `rules.reload` |
| `/rules reset  | Forces every player to re-read and accept the rules again | `rules.reset`  |

## Portal

Warp between pre-defined portals with particle effects and GUI dialogs. Includes BlueMap web map integration.

### Features

- Create and manage portal zones for teleportation
- Particle effects around portal zones (beacon, leaves, portal, end_rod, flame, soul_fire_flame, lava, water)
- GUI dialog for portal selection with favourites support
- Automatic teleportation when entering portal zones
- BlueMap web map integration with customizable icons
- Spawn portal for new players without bed spawn

### Commands

| Command                              | Description                                      | permissions            |
|--------------------------------------|--------------------------------------------------|------------------------|
| `/portal create [name]`              | Creates a portal at your location                | `portal.create`        |
| `/portal <name> delete`              | Deletes a portal                                 | `portal.delete`        |
| `/portal <name> edit location`       | Updates portal location to your position         | `portal.edit.location` |
| `/portal <name> edit name <newName>` | Renames a portal                                 | `portal.edit.name`     |
| `/portal <name> setAsSpawn`          | Sets portal as the default spawn for new players | `portal.setasspawn`    |
| `/portal <name> setExit`             | Sets portal exit point to your location          | `portal.setexit`       |
| `/portal <name> setEffect <type>`    | Sets particle effect type                        | `portal.seteffect`     |
| `/portal <name> setIcon <icon>`      | Sets BlueMap map icon                            | `portal.seticon`       |
| `/portal help`                       | Shows help for portal commands                   | `portal.use`           |

### Configuration

Default portal settings in `config.yml`:

- `default-width`: 5 blocks
- `default-height`: 3 blocks
- `default-depth`: 5 blocks
- `default-effect`: beacon
- `default-icon`: minecraft:ender_pearl

