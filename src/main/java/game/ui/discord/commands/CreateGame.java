package game.ui.discord.commands;

import game.Game;
import game.GameLobby;
import game.Player;
import game.components.enums.Expansion;
import game.components.enums.FoodType;
import game.components.subcomponents.BirdCard;
import game.components.subcomponents.BonusCard;
import game.components.subcomponents.Card;
import game.service.GameService;
import game.ui.discord.enumeration.Constants;
import game.ui.discord.enumeration.DiscordObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import util.StringUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CreateGame implements SlashCommand {

    private static final String name = "create_game";
    private static final String description = "Start a game of Wingspan";

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(name, description);
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        // TODO: get the default game channel
        GameLobby lobby = GameService.getInstance().createLobby(event.getUser(), null);

        MessageEmbed embed = buildLobbyEmbed(lobby);
        List<ActionRow> components = buildLobbyComponents(lobby);

        event.replyEmbeds(embed)
                .setComponents(components)
                .queue();
    }

    public static MessageEmbed buildLobbyEmbed(GameLobby lobby) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(0x1abc9c);

        String expansionNames = lobby.getExpansions().stream()
                .map(Expansion::getLabel)
                .collect(Collectors.joining(", "));
        if (expansionNames.isEmpty()) expansionNames = "None";
        embed.addField(Constants.LOBBY_EXPANSIONS_FIELD, expansionNames, false);

        embed.addField(Constants.LOBBY_BOARD_FIELD, lobby.isNectarBoard() ? "Nectar Board" : "Standard Board", true);
        embed.addField(Constants.LOBBY_SEED_FIELD, lobby.getSeed() == 0 ? "Random" : String.valueOf(lobby.getSeed()), true);
        embed.addField(Constants.LOBBY_TEST_DATA_FIELD, lobby.isTestData() ? "On" : "Off", true);

        if (lobby.isWaitingForPlayers()) {
            embed.setTitle("\uD83C\uDFB2 Game Lobby");
            String playerList = lobby.getPlayers().stream()
                    .map(User::getAsMention)
                    .collect(Collectors.joining("\n"));
            embed.addField(Constants.LOBBY_PLAYERS_FIELD + " (" + lobby.getPlayers().size() + "/" + lobby.getPlayerCount() + ")",
                    playerList, false);
        } else {
            embed.setTitle(Constants.LOBBY_TITLE);
            embed.addField("\uD83D\uDC65 Player Count", String.valueOf(lobby.getPlayerCount()), true);
        }

        return embed.build();
    }

    public static List<ActionRow> buildLobbyComponents(GameLobby lobby) {
        if (lobby.isWaitingForPlayers()) {
            return buildWaitingComponents(lobby);
        }
        return buildConfigComponents(lobby);
    }

    private static List<ActionRow> buildConfigComponents(GameLobby lobby) {
        String lobbyId = lobby.getLobbyId();

        // Row 1: Expansion multi-select menu
        List<SelectOption> expansionOptions = Arrays.stream(Expansion.values())
                .map(exp -> {
                    SelectOption opt = SelectOption.of(exp.getLabel(), exp.name());
                    if (lobby.getExpansions().contains(exp)) {
                        opt = opt.withDefault(true);
                    }
                    return opt;
                })
                .toList();

        StringSelectMenu expansionMenu = StringSelectMenu.create(DiscordObject.CREATE_GAME_EXPANSION_SELECT_MENU.name() + ":" + lobbyId)
                .setPlaceholder("Select expansions")
                .setMinValues(0)
                .setMaxValues(Expansion.values().length)
                .addOptions(expansionOptions)
                .build();

        // Row 2: Board type + Test data + Set seed buttons
        Button boardTypeButton = lobby.isNectarBoard()
                ? Button.success(DiscordObject.CREATE_GAME_BOARD_TYPE_BUTTON.name() + ":" + lobbyId, "\uD83C\uDF0D Nectar Board")
                : Button.secondary(DiscordObject.CREATE_GAME_BOARD_TYPE_BUTTON.name() + ":" + lobbyId, "\uD83C\uDF0D Standard Board");

        Button testDataButton = lobby.isTestData()
                ? Button.success(DiscordObject.CREATE_GAME_TEST_DATA_BUTTON.name() + ":" + lobbyId, "\uD83E\uDDEA Test Data")
                : Button.secondary(DiscordObject.CREATE_GAME_TEST_DATA_BUTTON.name() + ":" + lobbyId, "\uD83E\uDDEA Test Data");

        Button setSeedButton = Button.primary(DiscordObject.CREATE_GAME_SET_SEED_BUTTON.name() + ":" + lobbyId, "\uD83C\uDFB2 Set Seed");

        // Row 3: Player count - / + buttons
        Button decrementButton = Button.secondary(DiscordObject.CREATE_GAME_PLAYER_COUNT_DECREMENT.name() + ":" + lobbyId, "➖ Players")
                .withDisabled(lobby.getPlayerCount() <= 1);
        Button incrementButton = Button.secondary(DiscordObject.CREATE_GAME_PLAYER_COUNT_INCREMENT.name() + ":" + lobbyId, "➕ Players")
                .withDisabled(lobby.getPlayerCount() >= Constants.LOBBY_MAX_PLAYERS);

        // Row 4: Create Game button
        Button startButton = Button.success(DiscordObject.CREATE_GAME_START_BUTTON.name() + ":" + lobbyId, "\uD83D\uDE80 Create Game");

        return List.of(
                ActionRow.of(expansionMenu),
                ActionRow.of(boardTypeButton, testDataButton, setSeedButton),
                ActionRow.of(decrementButton, incrementButton),
                ActionRow.of(startButton)
        );
    }

    private static List<ActionRow> buildWaitingComponents(GameLobby lobby) {
        String lobbyId = lobby.getLobbyId();

        Button joinButton = Button.success(DiscordObject.CREATE_GAME_JOIN_BUTTON.name() + ":" + lobbyId, "✅ Join Game");
        Button leaveButton = Button.danger(DiscordObject.CREATE_GAME_LEAVE_BUTTON.name() + ":" + lobbyId, "❌ Leave Game");

        return List.of(
                ActionRow.of(joinButton, leaveButton)
        );
    }

    /**
     * Add test data for the player creating the game
     */
    public static void addTestData(Game game, Player player) {
        if (game.getPlayers().size() != 1) {
            return;
        }
        IntStream.range(0, (int) (Math.random() * 4) + 1).forEach(i -> player.getBoard().getForest().addBird(game.getBirdDeck().drawCard()));
        IntStream.range(0, (int) (Math.random() * 4) + 1).forEach(i -> player.getBoard().getGrassland().addBird(game.getBirdDeck().drawCard()));
        IntStream.range(0, (int) (Math.random() * 4) + 1).forEach(i -> player.getBoard().getWetland().addBird(game.getBirdDeck().drawCard()));
        List.of(FoodType.WORM, FoodType.SEED, FoodType.FRUIT, FoodType.FISH, FoodType.RODENT, FoodType.NECTAR).forEach(f -> player.getHand().getPantry().put(f, (int) (Math.random() * 5)));
        List<BirdCard> playedBirds = player.getBoard().getPlayedBirds();
        Collections.shuffle(playedBirds);
        playedBirds.subList(0, playedBirds.size()/2).forEach(b -> b.getNest().setNumberOfEggs((int) (Math.random() * 3)));
    }

    private EmbedBuilder getStartingHandBirdsEmbed(List<BirdCard> birds) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(StringUtil.replacePlaceholders("\u200D[bird] Birds starting hand"));
        embed.setColor(0x1abc9c);

        birds.sort(Comparator.comparing(Card::getName));
        for (int i = 1; i <= birds.size(); i++) {
            embed.addField(i + ". " + birds.get(i-1).getName(), StringUtil.replacePlaceholders(birds.get(i-1).getPower().getPowerText()), true);
        }
        return embed;
    }

    private EmbedBuilder getStartingHandBonusEmbed(List<BonusCard> bonusCards) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(StringUtil.replacePlaceholders("\u200D[bonus-card] Bonus starting hand"));
        embed.setColor(0x32cd32);

        bonusCards.sort(Comparator.comparing(Card::getName));
        for (int i = 1; i <= bonusCards.size(); i++) {
            embed.addField(i + ". " + bonusCards.get(i-1).getName(), StringUtil.replacePlaceholders(bonusCards.get(i-1).getCondition()), true);
        }
        return embed;
    }
}
