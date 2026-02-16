# Wingspan Discord Bot

A Discord bot that lets you play the [Wingspan](https://stonemaiergames.com/games/wingspan/) board game with friends, right inside a Discord server. Create games, draft starting hands, place birds on your board, and compete for the highest score — all through slash commands and interactive buttons.

> **Status:** In development. Game creation, starting hand drafting, board/feeder/tray display, and basic actions (Play bird, Gain food, Lay eggs, Draw cards) all functional. Bird powers, end-of-round scoring, and nectar are still in progress.

### Slash Commands

| Command               | Description                                    |
|-----------------------|------------------------------------------------|
| `/create_game`        | Create a new game and invite players           |
| `/pick_starting_hand` | Choose your bird, bonus and food to start with |
| `/take_turn`          | Take your turn (Play Bird, Gain Food, Lay Eggs, Draw Cards) |
| `/see_board`          | View a player's board                          |
| `/see_bird_feeder`    | View the birdfeeder dice                       |
| `/see_tray`           | View the 3 birds available in the tray         |
| `/get_active_games`   | List your ongoing games                        |

## Tech Stack

- **Java 19**
- **[JDA 5](https://github.com/discord-jda/JDA)** — Discord API wrapper ([Wiki](https://jda.wiki/) | [Javadoc](https://ci.dv8tion.net/job/JDA5/javadoc/))
- **[Jackson](https://github.com/FasterXML/jackson)** — JSON parsing for game data
- **[Lombok](https://projectlombok.org/)** — boilerplate reduction
- **[JUnit 5](https://junit.org/junit5/)** — testing
- **Maven** — build & dependency management

## Getting Started

### Prerequisites

- Java 19+
- Maven
- A [Discord bot application](https://discord.com/developers/applications) with a bot token

### Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/Touniouk/DiscordWingspan.git
   cd DiscordWingspan
   ```

2. Copy the config template and fill in your bot credentials:
   ```bash
   cp template.config.properties config.properties
   ```
   Edit `config.properties`:
   ```properties
   bot.token=YOUR_BOT_TOKEN
   bot.client_id=YOUR_CLIENT_ID
   bot.guild_id=YOUR_GUILD_ID
   bot.default_game_channel_id=YOUR_CHANNEL_ID
   ```

3. Build and run:
   ```bash
   mvn compile exec:java -Dexec.mainClass="game.ui.discord.DiscordBot"
   ```

### Regenerating Bird Data

The bird card database (`birds.json`) is generated from a Wingspan Excel spreadsheet. To regenerate it:

```bash
cd scripts
python3 -m venv .venv
source .venv/bin/activate
pip install openpyxl
python3 generate_birds_json.py
```

The script reads `src/main/resources/wingspan-20260128.xlsx` and writes `src/main/resources/birds.json`.

### Running Tests

```bash
mvn test
```

## Project Structure

```
src/main/java/
  game/
    Game.java                  # Game orchestrator & setup
    Player.java                # Player state & board
    components/                # Board, hand, decks, cards
      enums/                   # FoodType, Habitat, NestType, etc.
      subcomponents/           # BirdCard, BonusCard, Die
      meta/                    # Habitat impls, Power, Nest
    exception/                 # GameInputException
    service/                   # GameService, state machines
    ui/discord/                # Discord bot, slash commands, processors
  util/                        # Logger, StringUtil

src/main/resources/
  birds.json                   # Bird card database
  bonus.json                   # Bonus card definitions
  goals.json                   # End-of-round goal cards
  asset/png/                   # Food, habitat, and card images
```

## TODO

- [x] Implement playing bird
    - [x] Disable +- buttons when spending food
    - [ ] If bird can only go in one habitat, add it automatically
    - [ ] Add food cost to message
- [x] Implement gaining food
- [x] Implement laying eggs
- [x] Implement drawing cards
- [ ] Implement resource discard on basic actions
  - [x] Discard eggs to draw cards
  - [ ] Discard cards to gain food
  - [ ] Discard food to gain eggs
  - [ ] Discard cards to gain eggs
- [ ] Implement brown powers
- [ ] Implement white powers
- [ ] Implement pink powers
- [ ] Implement end of rounds
- [ ] Implement bonus card scoring
- [ ] Playing with nectar
- [ ] Nectar scoring

## License

[MIT](LICENSE) — Copyright (c) 2025 Touniouk
