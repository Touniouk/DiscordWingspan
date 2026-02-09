# Wingspan Discord Bot

A Discord bot that lets you play the [Wingspan](https://stonemaiergames.com/games/wingspan/) board game with friends, right inside a Discord server. Create games, draft starting hands, place birds on your board, and compete for the highest score — all through slash commands and interactive buttons.

> **Status:** Early development. Game creation, starting hand drafting, and board display are functional. Core gameplay (turn actions, powers, scoring) is still in progress.

## Features

- **Create & join games** with 1–5 players
- **Starting hand draft** — choose which birds and food to keep
- **Board display** — view any player's 3-habitat board with placed birds
- **Food cost validation** — supports wild food, either/or costs, and 2-for-1 conversions
- **Oceania expansion** support (Nectar board variant)

### Slash Commands

| Command | Description |
|---------|-------------|
| `/create_game` | Create a new game and invite players |
| `/take_turn` | Take your turn (WIP) |
| `/see_board` | View a player's board |
| `/get_active_games` | List your ongoing games |

## Tech Stack

- **Java 19**
- **[JDA 5](https://github.com/discord-jda/JDA)** — Discord API wrapper
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
    service/                   # GameService, state machines
    ui/discord/                # Discord bot, slash commands, processors
  util/                        # Logger, StringUtil

src/main/resources/
  birds.json                   # Bird card database
  bonus.json                   # Bonus card definitions
  goals.json                   # End-of-round goal cards
  asset/png/                   # Food, habitat, and card images
```

## License

[MIT](LICENSE) — Copyright (c) 2025 Touniouk
