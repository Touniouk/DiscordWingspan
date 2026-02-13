package game.ui.discord.commands;

import game.Game;
import game.Player;
import game.components.enums.FoodType;
import game.components.subcomponents.BirdCard;
import game.components.subcomponents.BonusCard;
import game.components.subcomponents.Card;
import game.service.DiscordBotService;
import game.service.GameService;
import game.ui.discord.DiscordBot;
import game.ui.discord.enumeration.DiscordObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import util.StringUtil;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CreateGame implements SlashCommand {

    private static final String name = "create_game";
    private static final String description = "Start a game of Wingspan";

    private static final String PARAM_PLAYER_1 = "player_1";
    private static final String PARAM_PLAYER_2 = "player_2";
    private static final String PARAM_PLAYER_3 = "player_3";
    private static final String PARAM_PLAYER_4 = "player_4";
    private static final String PARAM_PLAYER_5 = "player_5";
    private static final String PARAM_CHANNEL = "bot_channel";
    private static final String PARAM_TEST_BOARD = "test_board";

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(name, description)
                .addOption(OptionType.USER, PARAM_PLAYER_1, "The first player", true)
                .addOption(OptionType.USER, PARAM_PLAYER_2, "The second player", false)
                .addOption(OptionType.USER, PARAM_PLAYER_3, "The third player", false)
                .addOption(OptionType.USER, PARAM_PLAYER_4, "The fourth player", false)
                .addOption(OptionType.USER, PARAM_PLAYER_5, "The fifth player", false)
                .addOption(OptionType.CHANNEL, PARAM_CHANNEL, "Which channel to play the game in", false)
                .addOption(OptionType.BOOLEAN, PARAM_TEST_BOARD, "Test board with fake data", false);
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        List<User> playerList = Stream.of(PARAM_PLAYER_1, PARAM_PLAYER_2, PARAM_PLAYER_3, PARAM_PLAYER_4, PARAM_PLAYER_5)
                .map(event::getOption)
                .filter(Objects::nonNull)
                .map(OptionMapping::getAsUser)
                .toList();
        if (playerList.stream().anyMatch(User::isBot)) {
            event.reply("Bots can't play Wingspan!").setEphemeral(true).queue();
            return;
        }
        TextChannel gameChannel = Optional
                .ofNullable(event.getOption(PARAM_CHANNEL))
                .map(OptionMapping::getAsChannel)
                .map(GuildChannelUnion::asTextChannel)
                .orElse(DiscordBotService.getInstance().getJda().getTextChannelById(DiscordBot.DEFAULT_GAME_CHANNEL));
        boolean testData = Optional.ofNullable(event.getOption(PARAM_TEST_BOARD)).map(OptionMapping::getAsBoolean).orElse(false);

        Game game = GameService.getInstance().createGame(gameChannel, playerList);
        String gameId = game.getGameId();

        if (testData) {
            game.getPlayers().forEach(player -> addTestData(game, player));
        }

        Button takeTurnButton = Button.success(DiscordObject.PROMPT_PICK_HAND_BUTTON.name() + ":" + gameId, "\uD83D\uDC50 Pick Starting Hand");
        String playersAsMention = StringUtil.getListAsString(GameService.getInstance().getGame(gameId).getPlayers().stream().map(p -> p.getUser().getAsMention()).toList(), "");
        event.reply("Game `" + gameId + "` created with " + playersAsMention)
                .addActionRow(takeTurnButton)
                .queue();
    }

    /**
     * Add test data for the player creating the game
     */
    private void addTestData(Game game, Player player) {
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

    // FIXME: Potentially send the starting hands in the players DMs?
    private void sendStartingHandEmbeds(Player player) {
        EmbedBuilder startingHandBirdsEmbed = getStartingHandBirdsEmbed(player.getHand().getBirdCards());
        player.getUser().openPrivateChannel()
                .flatMap(channel -> channel.sendMessageEmbeds(startingHandBirdsEmbed.build()))
                .queue();

        EmbedBuilder startingHandBonusEmbed = getStartingHandBonusEmbed(player.getHand().getBonusCards());
        player.getUser().openPrivateChannel()
                .flatMap(channel -> channel.sendMessageEmbeds(startingHandBonusEmbed.build()))
                .queue();
    }

    private EmbedBuilder getStartingHandBirdsEmbed(List<BirdCard> birds) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(StringUtil.replacePlaceholders("️[bird] Birds starting hand"));
        embed.setColor(0x1abc9c);

        birds.sort(Comparator.comparing(Card::getName));
        for (int i = 1; i <= birds.size(); i++) {
            embed.addField(i + ". " + birds.get(i-1).getName(), StringUtil.replacePlaceholders(birds.get(i-1).getPower().getPowerText()), true);
        }
        return embed;
    }

    private EmbedBuilder getStartingHandBonusEmbed(List<BonusCard> bonusCards) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(StringUtil.replacePlaceholders("️[bonus-card] Bonus starting hand"));
        embed.setColor(0x32cd32);

        bonusCards.sort(Comparator.comparing(Card::getName));
        for (int i = 1; i <= bonusCards.size(); i++) {
            embed.addField(i + ". " + bonusCards.get(i-1).getName(), StringUtil.replacePlaceholders(bonusCards.get(i-1).getCondition()), true);
        }
        return embed;
    }
}
