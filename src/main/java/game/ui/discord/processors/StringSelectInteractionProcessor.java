package game.ui.discord.processors;

import game.Game;
import game.GameLobby;
import game.Player;
import game.components.enums.Expansion;
import game.components.enums.FoodType;
import game.components.enums.HabitatEnum;
import game.components.meta.BoardAction;
import game.components.meta.Habitat;
import game.components.subcomponents.BirdCard;
import game.components.subcomponents.BonusCard;
import game.components.subcomponents.Card;
import game.exception.GameInputException;
import game.service.DiscordBotService;
import game.service.GameService;
import game.ui.discord.commands.CreateGame;
import game.ui.discord.enumeration.Constants;
import game.ui.discord.enumeration.DiscordObject;
import game.ui.discord.enumeration.EmojiEnum;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import util.LogLevel;
import util.Logger;
import util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Routes incoming string select menu interactions to the appropriate handler
 * based on the component ID (starting hand selection, turn actions, bird placement, etc.).
 */
public class StringSelectInteractionProcessor {

    private static final Logger logger = new Logger(StringSelectInteractionProcessor.class, LogLevel.ALL);

    public static void handleCommand(StringSelectInteractionEvent event) {
        logSelected(event);

        String rawComponentId = event.getComponentId().split(":")[0];

        // Route lobby interactions before resolving game context
        if (rawComponentId.startsWith("CREATE_GAME_")) {
            handleLobbySelect(event);
            return;
        }

        Optional<DiscordBotService.GameContext> gameContextOptional = DiscordBotService.resolveGameContext(event);
        if (gameContextOptional.isEmpty()) return;
        DiscordBotService.GameContext gameContext = gameContextOptional.get();

        switch (DiscordObject.valueOf(gameContext.componentId())) {
            case PICK_STARTING_HAND_BIRD_SELECT_MENU -> pickStartingHandBirdSelectMenu(event, gameContext.game(), gameContext.player());
            case PICK_STARTING_HAND_BONUS_SELECT_MENU -> pickStartingHandBonusSelectMenu(event, gameContext.game(), gameContext.player());
            case PICK_STARTING_HAND_FOOD_SELECT_MENU -> pickStartingHandFoodSelectMenu(event, gameContext.game(), gameContext.player());
            case TAKE_TURN_ACTION_CHOICE_SELECT_MENU -> takeTurnActionChoiceSelectMenu(event, gameContext.game(), gameContext.player());
            case TAKE_TURN_ACTION_CHOICE_PLAY_BIRD_SELECT_BIRD_SUB_MENU -> takeTurnActionChoicePlayBirdSelectBirdSubMenu(event, gameContext.game(), gameContext.player());
            case TAKE_TURN_ACTION_CHOICE_PLAY_BIRD_PICK_HABITAT -> takeTurnActionChoicePlayBirdPickHabitat(event, gameContext.game(), gameContext.player());
            case TAKE_TURN_ACTION_CHOICE_PLAY_BIRD_REMOVE_EGGS -> takeTurnActionChoicePlayBirdRemoveEggs(event, gameContext.game(), gameContext.player());
            case TAKE_TURN_ACTION_CHOICE_DISCARD_EGGS_BIRD_SELECT_MENU -> discardEggFromBirdToDraw(event, gameContext.game(), gameContext.player());
            default -> logger.warn("Unmapped component: " + gameContext.componentId());
        }
    }

    private static void takeTurnActionChoicePlayBirdRemoveEggs(StringSelectInteractionEvent event, String newMessage, Game currentGame, Player currentPlayer) {
        takeTurnActionChoicePlayBirdRemoveEggs(event, newMessage, currentGame, currentPlayer, false);
    }

    private static void takeTurnActionChoicePlayBirdRemoveEggs(StringSelectInteractionEvent event, String newMessage, Game currentGame, Player currentPlayer, boolean skipped) {
        // If we picked the same bird twice, check that it has 2 eggs
        String[] message = newMessage.split("\n\n");
        String habitatName = message[3].substring(message[3].indexOf(Constants.CHOOSE_HABITAT) + Constants.CHOOSE_HABITAT.length());
        String birdName = message[1].substring(message[1].indexOf(Constants.CHOOSE_BIRD_TO_PLAY) + Constants.CHOOSE_BIRD_TO_PLAY.length());
        HabitatEnum habitatEnum = HabitatEnum.getHabitatFromJsonValue(habitatName);

        boolean twoEggsFromSameBird = event.getValues().contains(Constants.SAME_AGAIN) && event.getValues().size() == 2;
        List<BirdCard> birdsToRemoveEggsFrom = event.getValues()
                .stream()
                .map(bird -> currentPlayer.getBoard().getHabitat(habitatEnum)
                        .getBirds()
                        .stream()
                        .filter(b -> b.getName().equals(bird))
                        .findAny()
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();
        if (twoEggsFromSameBird && birdsToRemoveEggsFrom.get(0).getNest().getNumberOfEggs() < 2) {
            event.reply(birdsToRemoveEggsFrom.get(0).getName() + " doesn't have enough eggs").setEphemeral(true).queue();
            return;
        }

        // We've done all the steps and can now play the bird
        String removeEggsFromString = skipped ? Constants.NONE : StringUtil.getListAsString(event.getValues(), ", ");
        event.editMessage(message[0] + "\n\n" + // Pick action
                        message[1] + "\n\n" + // Choose bird
                        message[2] + "\n\n" + // Choose food
                        message[3] + "\n\n" + // Pick a Habitat
                        Constants.CHOOSE_BIRDS_TO_REMOVE_EGG + removeEggsFromString)
                .setComponents()
                .queue();

        BirdCard birdToPlay = currentPlayer.getHand().getBirdByName(birdName).orElse(null);
        GameService.getInstance().confirmPlayBird(currentGame, currentPlayer, birdToPlay, habitatEnum, birdsToRemoveEggsFrom, twoEggsFromSameBird ? 2 : 1);
    }

    private static void takeTurnActionChoicePlayBirdRemoveEggs(StringSelectInteractionEvent event, Game currentGame, Player currentPlayer) {
        takeTurnActionChoicePlayBirdRemoveEggs(event, event.getMessage().getContentRaw(), currentGame, currentPlayer);
    }

    private static void takeTurnActionChoicePlayBirdPickHabitat(StringSelectInteractionEvent event, Game currentGame, Player currentPlayer) {
        HabitatEnum habitatEnum = HabitatEnum.valueOf(event.getValues().get(0));
        Habitat habitat = currentPlayer.getBoard().getHabitat(habitatEnum);

        // Check if this habitat is full
        if (habitat.isHabitatFull()) {
            event.reply("Your " + habitatEnum.getJsonValue() + " already has " + Habitat.numberOfSpaceInHabitat + " in it").setEphemeral(true).queue();
            return;
        }

        int numberOfEggsToSpend = habitat.getNumberOfEggsToSpend();
        if (numberOfEggsToSpend == 0) {
            // Skip the egg step
            String[] message = event.getMessage().getContentRaw().split("\n\n");
            String newMessage = message[0] + "\n\n" + // Pick action
                            message[1] + "\n\n" + // Choose bird
                            message[2] + "\n\n" + // Choose food
                            Constants.CHOOSE_HABITAT + habitatEnum.getJsonValue() + "\n\n" +
                            Constants.CHOOSE_BIRDS_TO_REMOVE_EGG + " " + Constants.NONE;
            takeTurnActionChoicePlayBirdRemoveEggs(event, newMessage, currentGame, currentPlayer, true);
            return;
        }

        List<SelectOption> birdsToRemoveEggsFrom = new ArrayList<>(currentPlayer.getBoard().getPlayedBirdsWithEggs().stream().map(bird -> SelectOption.of(bird.getName(), bird.getName())).toList());
        if (numberOfEggsToSpend > 1) {
            birdsToRemoveEggsFrom.add(SelectOption.of(Constants.SAME_AGAIN, Constants.SAME_AGAIN));
        }
        StringSelectMenu selectMenu = StringSelectMenu.create(DiscordObject.TAKE_TURN_ACTION_CHOICE_PLAY_BIRD_REMOVE_EGGS.name() + ":" + currentGame.getGameId())
                .setPlaceholder("Which birds to remove " + numberOfEggsToSpend + " eggs from")
                .setMinValues(1)
                .setMaxValues(numberOfEggsToSpend)
                .addOptions(birdsToRemoveEggsFrom)
                .build();

        String[] message = event.getMessage().getContentRaw().split("\n\n");
        event.editMessage(message[0] + "\n\n" + // Pick action
                        message[1] + "\n\n" + // Choose bird
                        message[2] + "\n\n" + // Choose food
                        Constants.CHOOSE_HABITAT + habitatEnum.getJsonValue() + "\n\n" +
                        Constants.CHOOSE_BIRDS_TO_REMOVE_EGG)
                .setComponents(ActionRow.of(selectMenu))
                .queue();
    }

    private static void takeTurnActionChoiceSelectMenu(StringSelectInteractionEvent event, Game currentGame, Player currentPlayer) {
        try {
            BoardAction boardAction = BoardAction.valueOf(event.getValues().get(0));
            switch (boardAction) {
                case GAIN_FOOD -> gainFood(event, currentGame, currentPlayer);
                case LAY_EGGS -> layEggs(event, currentGame, currentPlayer);
                case PLAY_BIRD -> playBird(event, currentGame, currentPlayer);
                case DRAW_CARDS -> drawCards(event, currentGame, currentPlayer);
                default -> logger.warn("Unmapped action: " + boardAction);
            }
        } catch (GameInputException ex) {
            event.reply(ex.getMessage()).setEphemeral(true).queue();
        }
    }

    private static void gainFood(StringSelectInteractionEvent event, Game currentGame, Player currentPlayer) {
        int maxFood = currentPlayer.getBoard().getForest().getNumberOfFoodToGain();

        ButtonInteractionProcessor.DiscordMessage picker = ButtonInteractionProcessor.buildFeedPickerMessage(currentGame, maxFood, "");
        event.editMessage(picker.content())
                .setComponents(picker.components())
                .queue();
    }

    private static void layEggs(StringSelectInteractionEvent event, Game currentGame, Player currentPlayer) {
        List<BirdCard> allBirds = currentPlayer.getBoard().getPlayedBirds();
        if (allBirds.isEmpty()) {
            event.reply("You don't have any birds to lay eggs on").setEphemeral(true).queue();
            return;
        }

        currentPlayer.getHand().resetTempEggs();
        int maxEggs = currentPlayer.getBoard().getGrassland().getNumberOfEggsToLay();

        ButtonInteractionProcessor.DiscordMessage msg = ButtonInteractionProcessor.buildLayEggsHabitatMessage(currentGame, currentPlayer, maxEggs);
        event.editMessage(msg.content())
                .setComponents(msg.components())
                .queue();
    }

    private static void drawCards(StringSelectInteractionEvent event, Game currentGame, Player currentPlayer) {
        int maxDraw = currentPlayer.getBoard().getWetland().getNumberOfCardsToDraw();
        currentPlayer.getHand().resetTempDrawnBirds();
        currentPlayer.getBoard().getWetland().setNumberOfResourcesDiscarded(0);

        ButtonInteractionProcessor.DiscordMessage msg = ButtonInteractionProcessor.buildDrawCardsMessage(currentGame, currentPlayer, maxDraw, 0);
        event.editMessage(msg.content())
                .setComponents(msg.components())
                .queue();
    }

    private static void playBird(StringSelectInteractionEvent event, Game currentGame, Player currentPlayer) throws GameInputException {
        if (currentPlayer.getHand().getBirdCards().isEmpty()) {
            throw new GameInputException("You do not have any birds to play");
        }

        StringSelectMenu pickBirdSubmenu = StringSelectMenu.create(DiscordObject.TAKE_TURN_ACTION_CHOICE_PLAY_BIRD_SELECT_BIRD_SUB_MENU.name() + ":" + currentGame.getGameId())
                .setPlaceholder("Pick bird to play")
                .addOptions(currentPlayer.getHand().getBirdCards().stream().map(c -> SelectOption.of(c.getName(), c.getName())).toList())
                .build();

        event.editMessage(Constants.PICK_ACTION + BoardAction.PLAY_BIRD.getLabel() + "\n\n" + Constants.CHOOSE_BIRD_TO_PLAY + "\n\n")
                .setComponents(ActionRow.of(pickBirdSubmenu))
                .queue();
    }

    private static void takeTurnActionChoicePlayBirdSelectBirdSubMenu(StringSelectInteractionEvent event, Game currentGame, Player currentPlayer) {
        currentPlayer.getHand().resetTempPantry();

        String birdToPlay;
        try {
            birdToPlay = event.getValues().stream().findFirst().orElseThrow(() -> new GameInputException("No bird selected"));
        } catch (GameInputException ex) {
            event.reply(ex.getMessage()).setEphemeral(true).queue();
            return;
        }

        List<ActionRow> components = getChooseFoodSelector(currentGame, currentPlayer);

        event.editMessage(Constants.PICK_ACTION + BoardAction.PLAY_BIRD.getLabel() + "\n\n" +
                        Constants.CHOOSE_BIRD_TO_PLAY + birdToPlay + "\n\n" +
                        Constants.CHOOSE_FOOD_TO_USE + "\n" +
                        "Food used: " + EmojiEnum.getFoodAsEmojiList(currentPlayer.getHand().getTempPantrySpentFood()) + "\n" +
                        "Food in hand: " + EmojiEnum.getFoodAsEmojiList(currentPlayer.getHand().getTempPantryAvailableFood()))
                .setComponents(components)
                .queue();
    }

    /**
     * Builds the add/remove food button rows and submit button for food selection during bird placement.
     */
    public static List<ActionRow> getChooseFoodSelector(Game currentGame, Player currentPlayer) {
        List<Button> addFoodButtons = new ArrayList<>();
        List<Button> removeFoodButtons = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Button addFoodButton = Button.primary(DiscordObject.ADD_FOOD_IDS[i].name() + ":" + currentGame.getGameId(), "➕")
                    .withEmoji(Emoji.fromFormatted(FoodType.values()[i].getEmoji().getEmoteId()))
                    .withDisabled(currentPlayer.getHand().getTempPantryAvailableFood().get(FoodType.values()[i]) == 0);
            Button removeFoodButton = Button.danger(DiscordObject.REMOVE_FOOD_IDS[i].name() + ":" + currentGame.getGameId(), "➖")
                    .withEmoji(Emoji.fromFormatted(FoodType.values()[i].getEmoji().getEmoteId()))
                    .withDisabled(currentPlayer.getHand().getTempPantrySpentFood().get(FoodType.values()[i]) == 0);
            addFoodButtons.add(addFoodButton);
            removeFoodButtons.add(removeFoodButton);
        }

        return List.of(
                ActionRow.of(addFoodButtons),
                ActionRow.of(removeFoodButtons),
                ActionRow.of(Button.primary(DiscordObject.TAKE_TURN_ACTION_CHOICE_PLAY_BIRD_CHOOSE_FOOD_SUBMIT_BUTTON.name() + ":" + currentGame.getGameId(), Constants.SUBMIT_SELECTION))
        );
    }

    private static void pickStartingHandFoodSelectMenu(StringSelectInteractionEvent event, Game currentGame, Player currentPlayer) {
        currentPlayer.getHand().resetPantry();
        List<FoodType> foodSelected = event.getValues().stream().filter(f -> !f.equalsIgnoreCase("none")).map(FoodType::valueOf).toList();
        foodSelected.forEach(food -> currentPlayer.getHand().getPantry().put(food, 1));

        MessageEmbed embed = event.getMessage().getEmbeds().stream().findFirst().orElse(new EmbedBuilder()
                .setDescription(Constants.BIRDS_NOT_SELECTED + "\n" + Constants.FOOD_SELECTED + "\n" + Constants.BONUS_NOT_SELECTED)  // fallback if no embed
                .build());

        EmbedBuilder newEmbedBuilder = new EmbedBuilder(embed);
        newEmbedBuilder.getFields().removeIf(f -> Objects.equals(f.getName(), Constants.FOOD_SELECTED_FIELD));
        MessageEmbed newEmbed = newEmbedBuilder.setDescription(Objects.requireNonNull(embed.getDescription()).replace(Constants.FOOD_NOT_SELECTED, Constants.FOOD_SELECTED))
                .addField(Constants.FOOD_SELECTED_FIELD, foodSelected.isEmpty() ? "None" : StringUtil.getListAsString(foodSelected.stream().map(FoodType::getDisplayName), ", "), true)
                .build();

        String buttonId = DiscordObject.PICK_STARTING_HAND_SUBMIT_BUTTON.name() + ":" + currentGame.getGameId();
        List<ActionRow> newComponents = getNewComponents(event.getMessage(), newEmbed, buttonId);

        event.editMessageEmbeds(newEmbed)
                .setComponents(newComponents)
                .queue();
    }

    private static void pickStartingHandBirdSelectMenu(StringSelectInteractionEvent event, Game currentGame, Player currentPlayer) {
        currentPlayer.getHand().getBirdCards().forEach(c -> c.setSelected(false));
        List<BirdCard> selectedCards = currentPlayer.getHand()
                .getBirdCards()
                .stream()
                .filter(card -> event.getValues().contains(card.getName())).toList();
        selectedCards.forEach(card -> card.setSelected(true));

        MessageEmbed embed = event.getMessage().getEmbeds().stream().findFirst().orElse(new EmbedBuilder()
                .setDescription(Constants.BIRDS_SELECTED + "\n" + Constants.FOOD_NOT_SELECTED + "\n" + Constants.BONUS_NOT_SELECTED)  // fallback if no embed
                .build());

        EmbedBuilder newEmbedBuilder = new EmbedBuilder(embed);
        newEmbedBuilder.getFields().removeIf(f -> Objects.equals(f.getName(), Constants.BIRDS_SELECTED_FIELD));
        MessageEmbed newEmbed = newEmbedBuilder.setDescription(Objects.requireNonNull(embed.getDescription()).replace(Constants.BIRDS_NOT_SELECTED, Constants.BIRDS_SELECTED))
                .addField(Constants.BIRDS_SELECTED_FIELD, selectedCards.isEmpty() ? "None" : StringUtil.getListAsString(selectedCards.stream().map(Card::getName), ", "), true)
                .build();

        String buttonId = DiscordObject.PICK_STARTING_HAND_SUBMIT_BUTTON.name() + ":" + currentGame.getGameId();
        List<ActionRow> newComponents = getNewComponents(event.getMessage(), newEmbed, buttonId);

        event.editMessageEmbeds(newEmbed)
                .setComponents(newComponents)
                .queue();
    }

    private static void pickStartingHandBonusSelectMenu(StringSelectInteractionEvent event, Game currentGame, Player currentPlayer) {
        currentPlayer.getHand().getBonusCards().forEach(c -> c.setSelected(false));
        List<BonusCard> selectedCards = currentPlayer.getHand()
                .getBonusCards()
                .stream()
                .filter(card -> event.getValues().contains(card.getName())).toList();
        selectedCards.forEach(card -> card.setSelected(true));

        MessageEmbed embed = event.getMessage().getEmbeds().stream().findFirst().orElse(new EmbedBuilder()
                .setDescription(Constants.BIRDS_NOT_SELECTED + "\n" + Constants.FOOD_NOT_SELECTED + "\n" + Constants.BONUS_SELECTED)  // fallback if no embed
                .build());

        EmbedBuilder newEmbedBuilder = new EmbedBuilder(embed);
        newEmbedBuilder.getFields().removeIf(f -> Objects.equals(f.getName(), Constants.BONUS_SELECTED_FIELD));
        MessageEmbed newEmbed = newEmbedBuilder.setDescription(Objects.requireNonNull(embed.getDescription()).replace(Constants.BONUS_NOT_SELECTED, Constants.BONUS_SELECTED))
                .addField(Constants.BONUS_SELECTED_FIELD, selectedCards.isEmpty() ? "None" : StringUtil.getListAsString(selectedCards.stream().map(Card::getName), ", "), true)
                .build();

        String buttonId = DiscordObject.PICK_STARTING_HAND_SUBMIT_BUTTON.name() + ":" + currentGame.getGameId();
        List<ActionRow> newComponents = getNewComponents(event.getMessage(), newEmbed, buttonId);

        event.editMessageEmbeds(newEmbed)
                .setComponents(newComponents)
                .queue();
    }

    /**
     * Rebuilds the message components, enabling the submit button only when all three
     * starting hand categories (birds, food, bonus) have been selected.
     */
    public static List<ActionRow> getNewComponents(Message message, MessageEmbed newEmbed, String buttonId) {
        return message.getActionRows().stream()
                .map(row -> ActionRow.of(
                        row.getComponents().stream()
                                .map(component -> {
                                    if (component instanceof Button button && buttonId.equals(button.getId())) {
                                        // Enable only if all selected
                                        boolean enable = newEmbed.getDescription() != null &&
                                                newEmbed.getDescription().contains(Constants.BIRDS_SELECTED) &&
                                                newEmbed.getDescription().contains(Constants.FOOD_SELECTED) &&
                                                newEmbed.getDescription().contains(Constants.BONUS_SELECTED);
                                        return button.withDisabled(!enable);
                                    }
                                    return component;
                                }).toList()
                )).toList();
    }

    private static void discardEggFromBirdToDraw(StringSelectInteractionEvent event, Game game, Player player) {
        String birdName = event.getValues().get(0);

        // Find the bird across all habitats
        BirdCard bird = null;
        for (HabitatEnum habitat : HabitatEnum.values()) {
            for (BirdCard b : player.getBoard().getHabitat(habitat).getBirds()) {
                if (b.getName().equals(birdName)) {
                    bird = b;
                    break;
                }
            }
            if (bird != null) break;
        }

        if (bird == null || bird.getNest().getNumberOfEggs() <= 0) {
            event.reply("Could not find a bird with eggs matching: " + birdName).setEphemeral(true).queue();
            return;
        }

        // Remove 1 egg
        bird.getNest().setNumberOfEggs(bird.getNest().getNumberOfEggs() - 1);

        // Increment resources discarded on the wetland
        player.getBoard().getWetland().setNumberOfResourcesDiscarded(
                player.getBoard().getWetland().getNumberOfResourcesDiscarded() + 1);

        // Parse maxDraw from message and increment
        int maxDraw = ButtonInteractionProcessor.parseMaxDrawMessage(event.getMessage().getContentRaw()) + 1;

        // Extract tray buttons from the draw tray/deck row (row 0), preserving selection state
        ActionRow drawTrayRow = event.getMessage().getActionRows().get(0);
        int selectedCount = player.getHand().getTempDrawnBirds().size();
        List<Button> trayButtons = new ArrayList<>();
        for (ItemComponent component : drawTrayRow.getComponents()) {
            if (component instanceof Button button) {
                String id = Objects.requireNonNull(button.getId()).split(":")[0];
                if (!DiscordObject.TAKE_TURN_ACTION_CHOICE_DRAW_CARDS_DRAW_DECK.name().equals(id)) {
                    if (button.getStyle() == ButtonStyle.SUCCESS) selectedCount++;
                    trayButtons.add(button);
                }
            }
        }

        // Rebuild draw cards UI with increased maxDraw
        ButtonInteractionProcessor.DiscordMessage msg = ButtonInteractionProcessor.buildDrawCardsMessage(game, player, maxDraw, selectedCount, trayButtons);
        event.editMessage(msg.content())
                .setComponents(msg.components())
                .queue();
    }

    private static void logSelected(StringSelectInteractionEvent event) {
        logger.ridiculous(event.getUser().getName() + " selected " + event.getValues().stream()
                .map(String::toUpperCase)
                .collect(Collectors.joining(", ")));
    }

    // ======================== LOBBY HANDLERS ========================

    private static void handleLobbySelect(StringSelectInteractionEvent event) {
        Optional<DiscordBotService.LobbyContext> lobbyContextOptional = DiscordBotService.resolveLobbyContext(event);
        if (lobbyContextOptional.isEmpty()) return;
        DiscordBotService.LobbyContext ctx = lobbyContextOptional.get();

        if (event.getUser().getIdLong() != ctx.lobby().getCreator().getIdLong()) {
            event.reply("Only the game creator can change settings.").setEphemeral(true).queue();
            return;
        }

        if (DiscordObject.valueOf(ctx.componentId()) == DiscordObject.CREATE_GAME_EXPANSION_SELECT_MENU) {
            updateExpansions(event, ctx.lobby());
        } else {
            logger.warn("Unmatched lobby select: " + ctx.componentId());
        }
    }

    private static void updateExpansions(StringSelectInteractionEvent event, GameLobby lobby) {
        List<Expansion> selected = event.getValues().stream()
                .map(Expansion::valueOf)
                .collect(Collectors.toList());
        lobby.setExpansions(selected);
        event.editMessageEmbeds(CreateGame.buildLobbyEmbed(lobby))
                .setComponents(CreateGame.buildLobbyComponents(lobby))
                .queue();
    }
}
