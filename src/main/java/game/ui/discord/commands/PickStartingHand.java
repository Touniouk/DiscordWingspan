package game.ui.discord.commands;

import game.Game;
import game.Player;
import game.components.enums.FoodType;
import game.components.subcomponents.BirdCard;
import game.components.subcomponents.BonusCard;
import game.components.subcomponents.Card;
import game.service.DiscordBotService;
import game.ui.discord.enumeration.Constants;
import game.ui.discord.enumeration.DiscordObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class PickStartingHand implements SlashCommand {
    private static final String name = "pick_starting_hand";
    private static final String description = "Pick your starting hand";

    private static final String PARAM_GAME_ID = Constants.GAME_ID;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(name, description)
                .addOption(OptionType.STRING, PARAM_GAME_ID, "The game id (defaults to your active game)", false, true);
    }

    @Override
    public void handleAutoComplete(CommandAutoCompleteInteractionEvent event) {
        SlashCommand.autoCompleteGameId(event);
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        Optional<DiscordBotService.GameContext> gameContextOptional = DiscordBotService.resolveGameContext(event);
        if (gameContextOptional.isEmpty()) return;
        DiscordBotService.GameContext gameContext = gameContextOptional.get();

        sendStartingHand(event, gameContext.game(), gameContext.player());
    }

    public static void sendStartingHand(IReplyCallback event, Game game, Player player) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(Constants.CHOOSE_STARTING_HAND)
                .setDescription(Constants.BIRDS_NOT_SELECTED + "\n" + Constants.FOOD_NOT_SELECTED + "\n" + Constants.BONUS_NOT_SELECTED)
                .setColor(0x1abc9c);
        event.replyEmbeds(embed.build())
                .addActionRow(getStartingHandBirdsSelectMenu(player.getHand().getBirdCards(), game.getGameId()))
                .addActionRow(getStartingHandFoodSelectMenu(game.getGameId()))
                .addActionRow(getStartingHandBonusSelectMenu(player.getHand().getBonusCards(), game.getGameId()))
                .addActionRow(
                        Button.primary(DiscordObject.PICK_STARTING_HAND_SUBMIT_BUTTON.name() + ":" + game.getGameId(), Constants.SUBMIT_SELECTION)
                                .withDisabled(true),
                        Button.secondary(DiscordObject.PICK_STARTING_HAND_RANDOMISE_BUTTON.name() + ":" + game.getGameId(), "\uD83D\uDD00 Random Selection"))
                .setEphemeral(true)
                .queue();
    }

    private static StringSelectMenu getStartingHandBirdsSelectMenu(List<BirdCard> birds, String gameId) {
        return StringSelectMenu.create(DiscordObject.PICK_STARTING_HAND_BIRD_SELECT_MENU.name() + ":" + gameId)
                .setPlaceholder("Pick starting hand birds")
                .setMinValues(1)
                .setMaxValues(5)
                .addOption("None", "none")
                .addOptions(birds.stream().sorted(Comparator.comparing(Card::getName)).map(bird -> SelectOption.of(bird.getName(), bird.getName())).toList())
                .build();
    }

    private static StringSelectMenu getStartingHandFoodSelectMenu(String gameId) {
        return StringSelectMenu.create(DiscordObject.PICK_STARTING_HAND_FOOD_SELECT_MENU.name() + ":" + gameId)
                .setPlaceholder("Pick starting hand food")
                .setMinValues(1)
                .setMaxValues(5)
                .addOption("None", "none")
                .addOptions(FoodType.getStartingHandFoodTypes().stream().map(food -> SelectOption.of(food.getDisplayName(), food.name())).toList())
                .build();
    }

    private static StringSelectMenu getStartingHandBonusSelectMenu(List<BonusCard> bonusCards, String gameId) {
        return StringSelectMenu.create(DiscordObject.PICK_STARTING_HAND_BONUS_SELECT_MENU.name() + ":" + gameId)
                .setPlaceholder("Pick starting hand bonus cards")
                .addOptions(bonusCards.stream().sorted(Comparator.comparing(Card::getName)).map(bird -> SelectOption.of(bird.getName(), bird.getName())).toList())
                .build();
    }
}
