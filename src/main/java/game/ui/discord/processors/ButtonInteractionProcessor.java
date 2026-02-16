package game.ui.discord.processors;

import game.Game;
import game.GameLobby;
import game.Player;
import game.components.enums.Expansion;
import game.components.enums.FoodType;
import game.components.enums.HabitatEnum;
import game.components.meta.BoardAction;
import game.components.meta.Habitat;
import game.components.subcomponents.*;
import game.exception.GameInputException;
import game.service.DiscordBotService;
import game.service.GameService;
import game.ui.discord.commands.*;
import game.ui.discord.enumeration.Constants;
import game.ui.discord.enumeration.DiscordObject;
import game.ui.discord.enumeration.EmojiEnum;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import util.LogLevel;
import util.Logger;
import util.StringUtil;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Routes incoming button interactions to the appropriate handler based on the component ID.
 * Handles play-bird food selection, gain-food die picking, egg laying, and card drawing flows.
 */
public class ButtonInteractionProcessor {

    private static final Logger logger = new Logger(ButtonInteractionProcessor.class, LogLevel.ALL);

    public static void handleCommand(ButtonInteractionEvent event) {
        String rawComponentId = event.getComponentId().split(":")[0];

        // Route lobby interactions before resolving game context
        if (rawComponentId.startsWith("CREATE_GAME_")) {
            handleLobbyButton(event);
            return;
        }

        Optional<DiscordBotService.GameContext> gameContextOptional = DiscordBotService.resolveGameContext(event);
        if (gameContextOptional.isEmpty()) return;
        DiscordBotService.GameContext gameContext = gameContextOptional.get();
        long userId = event.getUser().getIdLong();

        try {
            switch (DiscordObject.valueOf(gameContext.componentId())) {
                // Generic buttons
                case PROMPT_PICK_HAND_BUTTON -> PickStartingHand.sendStartingHand(event, gameContext.game(), gameContext.player());
                case PROMPT_TAKE_TURN_BUTTON -> TakeTurn.takeTurn(event, gameContext.game(), gameContext.player());
                case PROMPT_SEE_BOARD_BUTTON -> SeeBoard.seeBoard(event, gameContext.player());
                case PROMPT_SEE_FEEDER_BUTTON -> SeeBirdFeeder.seeBirdFeeder(event, gameContext);
                case PROMPT_SEE_TRAY_BUTTON -> SeeTray.seeTray(event, gameContext.game());

                // Pick Starting hand buttons
                case PICK_STARTING_HAND_SUBMIT_BUTTON -> pickStartingHandSubmitButton(event, gameContext.game(), userId);
                case PICK_STARTING_HAND_RANDOMISE_BUTTON -> pickStartingHandRandomiseButton(event, gameContext.game(), gameContext.player());

                // Play bird buttons
                case TAKE_TURN_ACTION_CHOICE_PLAY_BIRD_CHOOSE_FOOD_ADD_WORM,
                     TAKE_TURN_ACTION_CHOICE_PLAY_BIRD_CHOOSE_FOOD_ADD_FISH,
                     TAKE_TURN_ACTION_CHOICE_PLAY_BIRD_CHOOSE_FOOD_ADD_SEED,
                     TAKE_TURN_ACTION_CHOICE_PLAY_BIRD_CHOOSE_FOOD_ADD_FRUIT,
                     TAKE_TURN_ACTION_CHOICE_PLAY_BIRD_CHOOSE_FOOD_ADD_RODENT,
                     TAKE_TURN_ACTION_CHOICE_PLAY_BIRD_CHOOSE_FOOD_ADD_NECTAR -> takeTurnActionChoicePlayBirdChooseFoodAddFood(event, gameContext);
                case TAKE_TURN_ACTION_CHOICE_PLAY_BIRD_CHOOSE_FOOD_REMOVE_WORM,
                     TAKE_TURN_ACTION_CHOICE_PLAY_BIRD_CHOOSE_FOOD_REMOVE_RODENT,
                     TAKE_TURN_ACTION_CHOICE_PLAY_BIRD_CHOOSE_FOOD_REMOVE_NECTAR,
                     TAKE_TURN_ACTION_CHOICE_PLAY_BIRD_CHOOSE_FOOD_REMOVE_FISH,
                     TAKE_TURN_ACTION_CHOICE_PLAY_BIRD_CHOOSE_FOOD_REMOVE_FRUIT,
                     TAKE_TURN_ACTION_CHOICE_PLAY_BIRD_CHOOSE_FOOD_REMOVE_SEED -> takeTurnActionChoicePlayBirdChooseFoodRemoveFood(event, gameContext);
                case TAKE_TURN_ACTION_CHOICE_PLAY_BIRD_CHOOSE_FOOD_SUBMIT_BUTTON -> takeTurnActionChoicePlayBirdChooseFoodSubmitButton(event, gameContext.gameId(), gameContext.player());

                // Gain food buttons
                case TAKE_TURN_ACTION_CHOICE_GAIN_FOOD_DIE_0,
                     TAKE_TURN_ACTION_CHOICE_GAIN_FOOD_DIE_1,
                     TAKE_TURN_ACTION_CHOICE_GAIN_FOOD_DIE_2,
                     TAKE_TURN_ACTION_CHOICE_GAIN_FOOD_DIE_3,
                     TAKE_TURN_ACTION_CHOICE_GAIN_FOOD_DIE_4 -> toggleGainFoodDie(event, gameContext.game());
                case TAKE_TURN_ACTION_CHOICE_GAIN_FOOD_SUBMIT_BUTTON -> submitGainFood(event, gameContext.game(), gameContext.player());
                case TAKE_TURN_ACTION_CHOICE_GAIN_FOOD_REROLL_BUTTON -> rerollFeeder(event, gameContext.game(), gameContext.player());

                // Lay Eggs buttons
                case TAKE_TURN_ACTION_CHOICE_LAY_EGGS_HABITAT_FOREST -> showLayEggsBirdsForHabitat(event, gameContext.game(), gameContext.player(), HabitatEnum.FOREST);
                case TAKE_TURN_ACTION_CHOICE_LAY_EGGS_HABITAT_GRASSLAND -> showLayEggsBirdsForHabitat(event, gameContext.game(), gameContext.player(), HabitatEnum.GRASSLAND);
                case TAKE_TURN_ACTION_CHOICE_LAY_EGGS_HABITAT_WETLAND -> showLayEggsBirdsForHabitat(event, gameContext.game(), gameContext.player(), HabitatEnum.WETLAND);
                case TAKE_TURN_ACTION_CHOICE_LAY_EGGS_ADD_BIRD_0,
                     TAKE_TURN_ACTION_CHOICE_LAY_EGGS_ADD_BIRD_1,
                     TAKE_TURN_ACTION_CHOICE_LAY_EGGS_ADD_BIRD_2,
                     TAKE_TURN_ACTION_CHOICE_LAY_EGGS_ADD_BIRD_3,
                     TAKE_TURN_ACTION_CHOICE_LAY_EGGS_ADD_BIRD_4 -> layEggsAddBird(event, gameContext.game(), gameContext.player());
                case TAKE_TURN_ACTION_CHOICE_LAY_EGGS_REMOVE_BIRD_0,
                     TAKE_TURN_ACTION_CHOICE_LAY_EGGS_REMOVE_BIRD_1,
                     TAKE_TURN_ACTION_CHOICE_LAY_EGGS_REMOVE_BIRD_2,
                     TAKE_TURN_ACTION_CHOICE_LAY_EGGS_REMOVE_BIRD_3,
                     TAKE_TURN_ACTION_CHOICE_LAY_EGGS_REMOVE_BIRD_4 -> layEggsRemoveBird(event, gameContext.game(), gameContext.player());
                case TAKE_TURN_ACTION_CHOICE_LAY_EGGS_BACK_BUTTON -> layEggsBackToHabitat(event, gameContext.game(), gameContext.player());
                case TAKE_TURN_ACTION_CHOICE_LAY_EGGS_SUBMIT_BUTTON -> submitLayEggs(event, gameContext.game(), gameContext.player());

                // Draw cards buttons
                case TAKE_TURN_ACTION_CHOICE_DRAW_CARDS_DRAW_TRAY_0,
                     TAKE_TURN_ACTION_CHOICE_DRAW_CARDS_DRAW_TRAY_1,
                     TAKE_TURN_ACTION_CHOICE_DRAW_CARDS_DRAW_TRAY_2 -> toggleTrayBirdSelected(event, gameContext.game(), gameContext.player());
                case TAKE_TURN_ACTION_CHOICE_DRAW_CARDS_DRAW_DECK -> drawCardFromDeck(event, gameContext.game(), gameContext.player());
                case TAKE_TURN_ACTION_DISCARD_EGGS -> discardEggToDrawCard(event, gameContext.game(), gameContext.player());
                case TAKE_TURN_ACTION_CHOICE_DRAW_CARDS_SUBMIT -> submitCardDrawSelection(event, gameContext.game(), gameContext.player());
                default -> logger.warn("Button id not matched: " + gameContext.componentId());
            }
        } catch (GameInputException ex) {
            event.reply(ex.getMessage()).setEphemeral(true).queue();
        }
    }

    record DiscordMessage(String content, List<ActionRow> components) {}

    // ======================== PLAY BIRD ========================

    private static void takeTurnActionChoicePlayBirdChooseFoodSubmitButton(ButtonInteractionEvent event, String gameId, Player currentPlayer) {
        // Check that we have the selected food
        boolean weHaveEnough = currentPlayer.getHand().getTempPantrySpentFood().entrySet().stream()
                .noneMatch(entry -> currentPlayer.getHand().getPantry().getOrDefault(entry.getKey(), 0) < entry.getValue());
        if (!weHaveEnough) {
            event.reply("You are trying to spend more food than you have").setEphemeral(true).queue();
            return;
        }

        // Check that the food we are spending covers the bird food cost
        String[] message = event.getMessage().getContentRaw().split("\n\n");
        String birdName = message[1].substring(message[1].indexOf(Constants.CHOOSE_BIRD_TO_PLAY) + Constants.CHOOSE_BIRD_TO_PLAY.length()).trim();
        BirdCard birdCard;
        try {
            birdCard = currentPlayer.getHand().getBirdByName(birdName)
                    .orElseThrow(() -> new GameInputException("You do not have *" + birdName + "* in hand"));
        } catch (GameInputException ex) {
            event.reply(ex.getMessage()).setEphemeral(true).queue();
            return;
        }
        logger.ridiculous("Bird food cost: " + birdCard.getFoodCost() + ", provided food: " + currentPlayer.getHand().getTempPantrySpentFood());
        int meetFoodCost = GameService.getInstance().checkFoodCost(birdCard.getFoodCost(), currentPlayer.getHand().getTempPantrySpentFood());
        if (meetFoodCost > 0) {
            event.reply("You must meet the bird food cost exactly. You've used too much food").setEphemeral(true).queue();
            return;
        } else if (meetFoodCost < 0) {
            event.reply("You must meet the bird food cost exactly. You've not used enough food").setEphemeral(true).queue();
            return;
        }

        StringSelectMenu selectMenu = StringSelectMenu.create(DiscordObject.TAKE_TURN_ACTION_CHOICE_PLAY_BIRD_PICK_HABITAT.name() + ":" + gameId)
                    .setPlaceholder("Pick the habitat")
                    .setMinValues(1)
                    .setMaxValues(1)
                    .addOptions(birdCard.getHabitats().stream().map(habitat -> SelectOption.of(habitat.getJsonValue(), habitat.name())).toList())
                    .build();

        event.editMessage(message[0] + "\n\n" +
                        message[1] + "\n\n" +
                        Constants.CHOOSE_FOOD_TO_USE + EmojiEnum.getFoodAsEmojiList(currentPlayer.getHand().getTempPantrySpentFood()) + "\n\n" +
                        Constants.CHOOSE_HABITAT)
                .setComponents(ActionRow.of(selectMenu))
                .queue();
    }

    private static void takeTurnActionChoicePlayBirdChooseFoodRemoveFood(ButtonInteractionEvent event, DiscordBotService.GameContext gameContext) throws GameInputException {
        int i;
        for (i = 0; i < DiscordObject.REMOVE_FOOD_IDS.length; i++) {
            if (DiscordObject.REMOVE_FOOD_IDS[i] == DiscordObject.valueOf(gameContext.componentId())) break;
        }

        int numberOfFoodToSpend = gameContext.player().getHand().getTempPantrySpentFood().get(FoodType.values()[i]);
        int numberOfFoodWeHave = gameContext.player().getHand().getTempPantryAvailableFood().get(FoodType.values()[i]);
        if (numberOfFoodToSpend <= 0) {
            throw new GameInputException("You cannot use a negative amount of food");
        }
        gameContext.player().getHand().getTempPantrySpentFood().put(FoodType.values()[i], numberOfFoodToSpend - 1);
        gameContext.player().getHand().getTempPantryAvailableFood().put(FoodType.values()[i], numberOfFoodWeHave + 1);

        showFoodUsedMessage(event, gameContext);
    }

    private static void takeTurnActionChoicePlayBirdChooseFoodAddFood(ButtonInteractionEvent event, DiscordBotService.GameContext gameContext) throws GameInputException {
        int i;
        for (i = 0; i < DiscordObject.ADD_FOOD_IDS.length; i++) {
            if (DiscordObject.ADD_FOOD_IDS[i] == DiscordObject.valueOf(gameContext.componentId())) break;
        }

        int numberOfFoodToSpend = gameContext.player().getHand().getTempPantrySpentFood().get(FoodType.values()[i]);
        int numberOfFoodWeHave = gameContext.player().getHand().getTempPantryAvailableFood().get(FoodType.values()[i]);
        if (numberOfFoodWeHave <= 0) {
            throw new GameInputException("You've used all your " + FoodType.values()[i].getDisplayName());
        }
        gameContext.player().getHand().getTempPantrySpentFood().put(FoodType.values()[i], numberOfFoodToSpend + 1);
        gameContext.player().getHand().getTempPantryAvailableFood().put(FoodType.values()[i], numberOfFoodWeHave - 1);

        showFoodUsedMessage(event, gameContext);
    }

    private static void showFoodUsedMessage(ButtonInteractionEvent event, DiscordBotService.GameContext gameContext) {
        String[] message = event.getMessage().getContentRaw().split("\n\n");

        List<ActionRow> components = StringSelectInteractionProcessor.getChooseFoodSelector(gameContext.game(), gameContext.player());

        event.editMessage(message[0] + "\n\n" + // Pick action...
                        message[1] + "\n\n" + // Chose to play bird...
                        Constants.CHOOSE_FOOD_TO_USE + "\n" +
                        "Food used: " + EmojiEnum.getFoodAsEmojiList(gameContext.player().getHand().getTempPantrySpentFood()) + "\n" +
                        "Food in hand: " + EmojiEnum.getFoodAsEmojiList(gameContext.player().getHand().getTempPantryAvailableFood()))
                .setComponents(components)
                .queue();
    }

    private static void pickStartingHandRandomiseButton(ButtonInteractionEvent event, Game currentGame, Player currentPlayer) {
        currentPlayer.getHand().getBonusCards().forEach(c -> c.setSelected(false));
        currentPlayer.getHand().getBirdCards().forEach(c -> c.setSelected(false));
        currentPlayer.getHand().resetPantry();

        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(i);
        }
        Collections.shuffle(list);
        List<FoodType> foodSelected = new ArrayList<>();
        List<BirdCard> selectedCards = new ArrayList<>();
        list.subList(0, 5).forEach(index -> {
            if (index < 5) {
                BirdCard birdCard = currentPlayer.getHand().getBirdCards().get(index);
                selectedCards.add(birdCard);
                birdCard.setSelected(true);
            } else {
                FoodType foodType = FoodType.values()[index-5];
                foodSelected.add(foodType);
                currentPlayer.getHand().getPantry().put(foodType, 1);
            }
        });
        BonusCard bonusCardSelected = currentPlayer.getHand().getBonusCards().get((int) (Math.random() * 2));
        bonusCardSelected.setSelected(true);

        MessageEmbed embed = event.getMessage().getEmbeds().stream().findFirst().orElse(new EmbedBuilder()
                .setDescription(Constants.BIRDS_SELECTED + "\n" + Constants.FOOD_SELECTED + "\n" + Constants.BONUS_SELECTED)  // fallback if no embed
                .build());

        EmbedBuilder newEmbedBuilder = new EmbedBuilder(embed);
        newEmbedBuilder.clearFields();
        MessageEmbed newEmbed = newEmbedBuilder.setDescription(Constants.BIRDS_SELECTED + "\n" + Constants.FOOD_SELECTED + "\n" + Constants.BONUS_SELECTED)
                .addField(Constants.FOOD_SELECTED_FIELD, foodSelected.isEmpty() ? "None" : StringUtil.getListAsString(foodSelected.stream().map(FoodType::getDisplayName), ", "), true)
                .addField(Constants.BIRDS_SELECTED_FIELD, selectedCards.isEmpty() ? "None" : StringUtil.getListAsString(selectedCards.stream().map(Card::getName), ", "), true)
                .addField(Constants.BONUS_SELECTED_FIELD, bonusCardSelected.getName(), true)
                .build();

        String buttonId = DiscordObject.PICK_STARTING_HAND_SUBMIT_BUTTON.name() + ":" + currentGame.getGameId();
        List<ActionRow> newComponents = StringSelectInteractionProcessor.getNewComponents(event.getMessage(), newEmbed, buttonId);

        event.editMessageEmbeds(newEmbed)
                .setComponents(newComponents)
                .queue();
    }

    private static void pickStartingHandSubmitButton(ButtonInteractionEvent event, Game currentGame, long userId) {
        String description = event.getMessage().getEmbeds().stream().findFirst().map(MessageEmbed::getDescription).orElse("");
        if (!description.contains(Constants.BIRDS_SELECTED)) {
            event.reply("Please select your birds first").setEphemeral(true).queue();
            return;
        }
        if (!description.contains(Constants.FOOD_SELECTED)) {
            event.reply("Please select your food first").setEphemeral(true).queue();
            return;
        }
        if (!description.contains(Constants.BONUS_SELECTED)) {
            event.reply("Please select your bonus first").setEphemeral(true).queue();
            return;
        }
        try {
            GameService.getInstance().confirmStartingHandPick(currentGame.getGameId(), userId);
        } catch (GameInputException e) {
            event.reply(e.getMessage()).setEphemeral(true).queue();
            return;
        }
        event.deferEdit().queue(hook -> hook.editOriginalComponents().queue());
        currentGame.getGameChannel().sendMessage(
                event.getUser().getAsMention() + " confirmed their starting hand"
        ).queue();
        GameService.getInstance().checkAllPlayersReady(currentGame);
    }

    // ======================== GAIN FOOD ========================

    private static int parseMaxFoodFromMessage(String content) {
        Matcher matcher = Pattern.compile("Pick up to \\*\\*(\\d+)\\*\\* food").matcher(content);
        int maxFood = matcher.find() ? Integer.parseInt(matcher.group(1)) : 1;
        logger.debug("Max food found in message : " + maxFood);
        return maxFood;
    }

    private static String parsePreviouslyGainedFood(String content) {
        int feederIdx = content.indexOf(Constants.CHOOSE_FOOD_FROM_FEEDER);
        if (feederIdx < 0) return "";
        String afterFeeder = content.substring(feederIdx + Constants.CHOOSE_FOOD_FROM_FEEDER.length());
        String[] lines = afterFeeder.split("\n");
        // Line 0: "Pick up to **N** food"
        // Line 1 (optional): dual-food hint text
        // Remaining lines: accumulated food emojis
        int emojiStart = 1;
        if (emojiStart < lines.length && lines[emojiStart].startsWith("Click dual-food")) {
            emojiStart = 2;
        }
        StringBuilder result = new StringBuilder();
        for (int i = emojiStart; i < lines.length; i++) {
            if (!lines[i].isBlank()) {
                if (!result.isEmpty()) result.append(" ");
                result.append(lines[i].trim());
            }
        }
        return result.toString();
    }

    private static void toggleGainFoodDie(ButtonInteractionEvent event, Game game) {
        String clickedId = event.getComponentId();
        String messageContent = event.getMessage().getContentRaw();
        int maxFood = parseMaxFoodFromMessage(messageContent);

        List<ActionRow> oldRows = event.getMessage().getActionRows();
        ActionRow dieRow = oldRows.get(0);
        ActionRow submitRow = oldRows.get(1);

        // Toggle the clicked button
        List<ItemComponent> newDieButtons = new ArrayList<>();
        int selectedCount = 0;
        for (ItemComponent component : dieRow.getComponents()) {
            if (component instanceof Button button) {
                boolean isClicked = clickedId.equals(button.getId());
                ButtonStyle currentStyle = button.getStyle();
                if (isClicked) {
                    String buttonComponentId = Objects.requireNonNull(button.getId()).split(":")[0];
                    int dieIndex = resolveDieIndex(buttonComponentId);
                    DieFace visibleFace = game.getFeeder().getDiceInFeeder().get(dieIndex).getVisibleFace();

                    if (visibleFace.isDualFood()) {
                        // 3-state cycle: SECONDARY → SUCCESS+food0 (1/2) → SUCCESS+food1 (2/2) → SECONDARY
                        List<FoodType> foods = visibleFace.getFoodType();
                        Button newButton;
                        if (currentStyle == ButtonStyle.SECONDARY) {
                            // Unselected → select food 0
                            newButton = Button.success(button.getId(), foods.get(0).getDisplayName() + " (1/2)")
                                    .withEmoji(Emoji.fromFormatted(foods.get(0).getEmoji().getEmoteId()));
                        } else if (button.getLabel().startsWith(foods.get(0).getDisplayName())) {
                            // Food 0 selected → select food 1
                            newButton = Button.success(button.getId(), foods.get(1).getDisplayName() + " (2/2)")
                                    .withEmoji(Emoji.fromFormatted(foods.get(1).getEmoji().getEmoteId()));
                        } else {
                            // Food 1 selected → back to unselected
                            newButton = Button.secondary(button.getId(), visibleFace.getLabel())
                                    .withEmoji(Emoji.fromFormatted(EmojiEnum.getFirstEmojiFromDieFace(visibleFace)));
                        }
                        newDieButtons.add(newButton);
                        if (newButton.getStyle() == ButtonStyle.SUCCESS) selectedCount++;
                    } else {
                        // Single-food: 2-state toggle
                        ButtonStyle newStyle = (currentStyle == ButtonStyle.SUCCESS) ? ButtonStyle.SECONDARY : ButtonStyle.SUCCESS;
                        Button newButton = (newStyle == ButtonStyle.SUCCESS)
                                ? Button.success(button.getId(), button.getLabel())
                                : Button.secondary(button.getId(), button.getLabel());
                        if (button.getEmoji() != null) {
                            newButton = newButton.withEmoji(button.getEmoji());
                        }
                        newDieButtons.add(newButton);
                        if (newStyle == ButtonStyle.SUCCESS) selectedCount++;
                    }
                } else {
                    newDieButtons.add(button);
                    if (currentStyle == ButtonStyle.SUCCESS) selectedCount++;
                }
            }
        }

        // If at max: disable unselected buttons; otherwise enable all
        List<ItemComponent> finalDieButtons = new ArrayList<>();
        for (ItemComponent component : newDieButtons) {
            if (component instanceof Button button) {
                if (selectedCount >= maxFood && button.getStyle() == ButtonStyle.SECONDARY) {
                    finalDieButtons.add(button.asDisabled());
                } else {
                    finalDieButtons.add(button.asEnabled());
                }
            }
        }

        // Enable re-roll only when feeder is in a re-roll-able state
        // Use the new button states (after toggling) to determine selected indices
        List<Integer> selectedIndicesForReroll = new ArrayList<>();
        for (ItemComponent comp : newDieButtons) {
            if (comp instanceof Button btn && btn.getStyle() == ButtonStyle.SUCCESS) {
                String btnCompId = Objects.requireNonNull(btn.getId()).split(":")[0];
                selectedIndicesForReroll.add(resolveDieIndex(btnCompId));
            }
        }

        // Consider those dice removed and check if you can reroll
        List<DieFace> dieFaces = IntStream.range(0, game.getFeeder().getDiceInFeeder().size())
                .filter(i -> !selectedIndicesForReroll.contains(i))
                .mapToObj(i -> game.getFeeder().getDiceInFeeder().get(i).getVisibleFace())
                .toList();
        boolean canReroll = dieFaces.isEmpty() || dieFaces.stream().distinct().count() == 1;
        List<ItemComponent> bottomRowButtons = new ArrayList<>();
        for (ItemComponent component : submitRow.getComponents()) {
            if (component instanceof Button button) {
                String id = Objects.requireNonNull(button.getId()).split(":")[0];
                if (id.equals(DiscordObject.TAKE_TURN_ACTION_CHOICE_GAIN_FOOD_REROLL_BUTTON.name())) {
                    bottomRowButtons.add(canReroll ? button.asEnabled() : button.asDisabled());
                } else {
                    bottomRowButtons.add(button);
                }
            }
        }

        event.editMessage(messageContent)
                .setComponents(ActionRow.of(finalDieButtons), ActionRow.of(bottomRowButtons))
                .queue();
    }

    static DiscordMessage buildFeedPickerMessage(Game game, int maxFood, String foodGainedSoFar) {
        List<Die> dice = game.getFeeder().getDiceInFeeder();
        String gameId = game.getGameId();

        List<Button> dieButtons = new ArrayList<>();
        for (int i = 0; i < dice.size(); i++) {
            DieFace face = dice.get(i).getVisibleFace();
            dieButtons.add(Button.secondary(DiscordObject.GAIN_FOOD_DIE_IDS[i].name() + ":" + gameId, face.getLabel())
                    .withEmoji(Emoji.fromFormatted(EmojiEnum.getFirstEmojiFromDieFace(face))));
        }

        Button submitButton = Button.success(DiscordObject.TAKE_TURN_ACTION_CHOICE_GAIN_FOOD_SUBMIT_BUTTON.name() + ":" + gameId, Constants.SUBMIT_SELECTION);
        Button rerollButton = Button.primary(DiscordObject.TAKE_TURN_ACTION_CHOICE_GAIN_FOOD_REROLL_BUTTON.name() + ":" + gameId, "\uD83C\uDFB2 Reroll Feeder")
                .withDisabled(game.getFeeder().canBeRerolled());

        boolean hasDualFoodDie = dice.stream().anyMatch(die -> die.getVisibleFace().isDualFood());
        String message = Constants.PICK_ACTION + "Gain Food\n\n" +
                Constants.CHOOSE_FOOD_FROM_FEEDER + "Pick up to **" + maxFood + "** food";
        if (hasDualFoodDie) {
            message += "\nClick dual-food dice again to cycle between food types";
        }
        if (!foodGainedSoFar.isEmpty()) {
            message += "\n" + foodGainedSoFar;
        }

        List<ActionRow> rows = List.of(ActionRow.of(dieButtons), ActionRow.of(submitButton, rerollButton));
        return new DiscordMessage(message, rows);
    }

    private static int resolveDieIndex(String buttonComponentId) {
        for (int i = 0; i < DiscordObject.GAIN_FOOD_DIE_IDS.length; i++) {
            if (DiscordObject.GAIN_FOOD_DIE_IDS[i].name().equals(buttonComponentId)) {
                return i;
            }
        }
        return 0;
    }

    /**
     * Inspects the die buttons in the message to determine which dice the player selected
     * and which food type they chose for each (relevant for dual-food dice).
     *
     * @param event the button interaction containing the die buttons
     * @return a map of die index to the chosen {@link FoodType}, preserving insertion order
     */
    private static Map<Integer, FoodType> resolveSelectedFoodChoices(ButtonInteractionEvent event) {
        ActionRow dieRow = event.getMessage().getActionRows().get(0);
        Map<Integer, FoodType> choices = new LinkedHashMap<>();
        for (ItemComponent component : dieRow.getComponents()) {
            if (component instanceof Button button && button.getStyle() == ButtonStyle.SUCCESS) {
                String buttonComponentId = Objects.requireNonNull(button.getId()).split(":")[0];
                int dieIndex = resolveDieIndex(buttonComponentId);
                // Strip " (1/2)" or " (2/2)" suffix from dual-food labels
                String label = button.getLabel().replaceAll(" \\(\\d/\\d\\)$", "");
                FoodType chosenFood = FoodType.fromDisplayName(label);
                choices.put(dieIndex, chosenFood);
            }
        }
        return choices;
    }

    /**
     * Submits the Gain Food action. Resolves the player's die selections, removes the dice
     * from the feeder, adds the food to the player's pantry (including any food gained from
     * prior rerolls), and ends the turn.
     *
     * @param event         the button interaction event
     * @param currentGame   the current game
     * @param currentPlayer the player gaining food
     */
    private static void submitGainFood(ButtonInteractionEvent event, Game currentGame, Player currentPlayer) {
        Map<Integer, FoodType> foodChoices = resolveFoodChoices(event, currentGame);
        String allFood = accumulateFoodGained(event, currentPlayer, foodChoices);

        event.editMessage(Constants.PICK_ACTION + "Gain Food\n\n" +
                        Constants.CHOOSE_FOOD_FROM_FEEDER + allFood)
                .setComponents()
                .queue();

        GameService.getInstance().endTurn(currentGame, currentPlayer);
    }

    /**
     * Resolves the player's selected food choices and removes the corresponding dice from the feeder.
     *
     * @param event       the button interaction event
     * @param currentGame the current game whose feeder dice are removed
     * @return a map of die index to chosen {@link FoodType}
     */
    private static Map<Integer, FoodType> resolveFoodChoices(ButtonInteractionEvent event, Game currentGame) {
        // Resolve food choices from button labels BEFORE removing dice
        Map<Integer, FoodType> foodChoices = resolveSelectedFoodChoices(event);
        List<Integer> selectedIndices = new ArrayList<>(foodChoices.keySet());

        // Remove dice from feeder (highest index first to avoid shifting)
        currentGame.getFeeder().getDice(selectedIndices);

        return foodChoices;
    }

    /**
     * Adds the chosen food to the player's pantry and builds a string of all food emoji gained,
     * including any food accumulated from previous rerolls (parsed from the message content).
     *
     * @param event       the button interaction event whose message may contain previously gained food
     * @param currentPlayer the player whose pantry receives the food
     * @param foodChoices the food types chosen in the current pick
     * @return a space-separated string of food emojis representing all food gained across all picks
     */
    private static String accumulateFoodGained(ButtonInteractionEvent event, Player currentPlayer, Map<Integer, FoodType> foodChoices) {
        // Accumulate food gained across re-rolls
        String previousFood = parsePreviouslyGainedFood(event.getMessage().getContentRaw());
        StringBuilder foodGained = new StringBuilder();
        for (FoodType foodType : foodChoices.values()) {
            currentPlayer.getHand().getPantry().merge(foodType, 1, Integer::sum);
            foodGained.append(foodType.getEmoji().getEmoteId()).append(" ");
        }
        return (previousFood + " " + foodGained.toString().trim()).trim();
    }

    /**
     * Handles a feeder reroll. Resolves the player's current die selections, removes those dice,
     * adds the food to the player's pantry, reduces the remaining pick count, rerolls the feeder,
     * and presents a new feed picker message with all previously gained food still displayed.
     *
     * @param event         the button interaction event
     * @param currentGame   the current game
     * @param currentPlayer the player re-rolling the feeder
     */
    private static void rerollFeeder(ButtonInteractionEvent event, Game currentGame, Player currentPlayer) {

        Map<Integer, FoodType> foodChoices = resolveFoodChoices(event, currentGame);
        String allFood = accumulateFoodGained(event, currentPlayer, foodChoices);
        List<Integer> selectedIndices = new ArrayList<>(foodChoices.keySet());

        // Update the max amount
        int maxFood = parseMaxFoodFromMessage(event.getMessage().getContentRaw());
        maxFood -= selectedIndices.size();

        // Actually reset the feeder
        currentGame.getFeeder().reRollFeeder();

        DiscordMessage picker = buildFeedPickerMessage(currentGame, maxFood, allFood);
        event.editMessage(picker.content())
                .setComponents(picker.components())
                .queue();
    }

    // ======================== LAY EGGS ========================

    static DiscordMessage buildLayEggsHabitatMessage(Game game, Player player, int maxEggs) {
        String gameId = game.getGameId();
        int eggsRemaining = maxEggs - player.getHand().getTotalTempEggs();

        StringBuilder content = new StringBuilder();
        content.append(Constants.PICK_ACTION).append(BoardAction.LAY_EGGS.getLabel()).append("\n\n");
        content.append(Constants.CHOOSE_EGGS_TO_LAY + "Lay up to **").append(maxEggs).append("** on your birds\n");
        content.append(Constants.LAY_EGGS_REMAINING).append(eggsRemaining).append("\n\n");

        HabitatEnum[] habitats = { HabitatEnum.FOREST, HabitatEnum.GRASSLAND, HabitatEnum.WETLAND };
        boolean[] habitatHasRoom = new boolean[3];

        // Create the habitat display
        for (int h = 0; h < habitats.length; h++) {
            Habitat habitat = player.getBoard().getHabitat(habitats[h]);
            List<BirdCard> birds = habitat.getBirds();
            content.append(habitats[h].getEmoji().getEmoteId()).append(" ").append(habitats[h].getJsonValue()).append(": ");
            if (birds.isEmpty()) {
                content.append("(empty)");
            } else {
                List<String> birdsInHabitat = new ArrayList<>();
                for (BirdCard bird : birds) {
                    int currentEggsOnBird = bird.getNest().getNumberOfEggs() + player.getHand().getTempEggsForBird(bird);
                    int cap = bird.getNest().getCapacity();
                    birdsInHabitat.add(bird.getName() + " (" + currentEggsOnBird + "/" + cap + " " + EmojiEnum.EGG.getEmoteId() + ")");
                    if (currentEggsOnBird < cap) habitatHasRoom[h] = true;
                }
                content.append(String.join(", ", birdsInHabitat));
            }
            content.append("\n");
        }

        // Create the habitat buttons
        List<Button> habitatButtons = new ArrayList<>();
        for (int h = 0; h < habitats.length; h++) {
            Habitat habitat = player.getBoard().getHabitat(habitats[h]);
            Button btn = Button.primary(DiscordObject.LAY_EGGS_HABITAT_IDS[h].name() + ":" + gameId, habitats[h].getJsonValue())
                    .withEmoji(Emoji.fromFormatted(habitats[h].getEmoji().getEmoteId()))
                    .withDisabled(habitat.getBirds().isEmpty() || !habitatHasRoom[h] || eggsRemaining <= 0);
            habitatButtons.add(btn);
        }

        Button submitButton = Button.success(DiscordObject.TAKE_TURN_ACTION_CHOICE_LAY_EGGS_SUBMIT_BUTTON.name() + ":" + gameId, Constants.SUBMIT_SELECTION);

        List<ActionRow> rows = new ArrayList<>();
        rows.add(ActionRow.of(habitatButtons));
        rows.add(ActionRow.of(submitButton));

        return new DiscordMessage(content.toString(), rows);
    }

    private static int parseMaxEggsFromMessage(String content) {
        Matcher matcher = Pattern.compile("Lay up to \\*\\*(\\d+)\\*\\* eggs").matcher(content);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 2;
    }

    private static HabitatEnum parseCurrentHabitatFromMessage(String content) {
        // Look for the habitat header line like "🌲 Forest birds:"
        if (content.contains("Forest birds:")) return HabitatEnum.FOREST;
        if (content.contains("Grassland birds:")) return HabitatEnum.GRASSLAND;
        if (content.contains("Wetland birds:")) return HabitatEnum.WETLAND;
        return null;
    }

    private static void showLayEggsBirdsForHabitat(ButtonInteractionEvent event, Game game, Player player, HabitatEnum habitatEnum) {
        int maxEggs = parseMaxEggsFromMessage(event.getMessage().getContentRaw());
        DiscordMessage msg = buildLayEggsBirdMessage(game, player, habitatEnum, maxEggs);
        event.editMessage(msg.content())
                .setComponents(msg.components())
                .queue();
    }

    private static DiscordMessage buildLayEggsBirdMessage(Game game, Player player, HabitatEnum habitatEnum, int maxEggs) {
        String gameId = game.getGameId();
        Habitat habitat = player.getBoard().getHabitat(habitatEnum);
        List<BirdCard> birds = habitat.getBirds();
        int eggsRemaining = maxEggs - player.getHand().getTotalTempEggs();

        StringBuilder content = new StringBuilder();
        content.append(Constants.PICK_ACTION).append(BoardAction.LAY_EGGS.getLabel()).append("\n\n");
        content.append(Constants.CHOOSE_EGGS_TO_LAY).append(maxEggs).append("** eggs on your birds\n");
        content.append(Constants.LAY_EGGS_REMAINING).append(eggsRemaining).append("\n\n");
        content.append(habitatEnum.getEmoji().getEmoteId()).append(" ").append(habitatEnum.getJsonValue()).append(" birds:\n");

        List<String> birdsInHabitat = new ArrayList<>();
        for (BirdCard bird : birds) {
            int current = bird.getNest().getNumberOfEggs() + player.getHand().getTempEggsForBird(bird);
            int cap = bird.getNest().getCapacity();
            birdsInHabitat.add(bird.getName() + " (" + current + "/" + cap + " " + EmojiEnum.EGG.getEmoteId() + ")");
        }
        content.append(String.join(" | ", birdsInHabitat));

        // Build add buttons row
        List<Button> addButtons = new ArrayList<>();
        for (int i = 0; i < birds.size(); i++) {
            BirdCard bird = birds.get(i);
            int current = bird.getNest().getNumberOfEggs() + player.getHand().getTempEggsForBird(bird);
            Button btn = Button.primary(DiscordObject.LAY_EGGS_ADD_IDS[i].name() + ":" + gameId, "➕ " + bird.getName());
            if (current >= bird.getNest().getCapacity() || eggsRemaining <= 0) {
                btn = btn.asDisabled();
            }
            addButtons.add(btn);
        }

        // Build remove buttons row
        List<Button> removeButtons = new ArrayList<>();
        for (int i = 0; i < birds.size(); i++) {
            BirdCard bird = birds.get(i);
            int tempEggs = player.getHand().getTempEggsForBird(bird);
            Button btn = Button.danger(DiscordObject.LAY_EGGS_REMOVE_IDS[i].name() + ":" + gameId, "➖ " + bird.getName());
            if (tempEggs <= 0) {
                btn = btn.asDisabled();
            }
            removeButtons.add(btn);
        }

        // Control row
        Button backButton = Button.secondary(DiscordObject.TAKE_TURN_ACTION_CHOICE_LAY_EGGS_BACK_BUTTON.name() + ":" + gameId, "\uD83D\uDD19 Back");
        Button submitButton = Button.success(DiscordObject.TAKE_TURN_ACTION_CHOICE_LAY_EGGS_SUBMIT_BUTTON.name() + ":" + gameId, Constants.SUBMIT_SELECTION);

        List<ActionRow> rows = new ArrayList<>();
        rows.add(ActionRow.of(addButtons));
        rows.add(ActionRow.of(removeButtons));
        rows.add(ActionRow.of(backButton, submitButton));

        return new DiscordMessage(content.toString(), rows);
    }

    private static int resolveBirdIndex(String buttonComponentId) {
        for (int i = 0; i < DiscordObject.LAY_EGGS_ADD_IDS.length; i++) {
            if (DiscordObject.LAY_EGGS_ADD_IDS[i].name().equals(buttonComponentId)) return i;
        }
        for (int i = 0; i < DiscordObject.LAY_EGGS_REMOVE_IDS.length; i++) {
            if (DiscordObject.LAY_EGGS_REMOVE_IDS[i].name().equals(buttonComponentId)) return i;
        }
        return 0;
    }

    private static void layEggsAddBird(ButtonInteractionEvent event, Game game, Player player) {
        String buttonComponentId = event.getComponentId().split(":")[0];
        int birdIndex = resolveBirdIndex(buttonComponentId);
        HabitatEnum habitatEnum = parseCurrentHabitatFromMessage(event.getMessage().getContentRaw());
        if (habitatEnum == null) return;

        List<BirdCard> birds = player.getBoard().getHabitat(habitatEnum).getBirds();
        if (birdIndex >= birds.size()) return;

        BirdCard bird = birds.get(birdIndex);
        int maxEggs = parseMaxEggsFromMessage(event.getMessage().getContentRaw());
        int eggsRemaining = maxEggs - player.getHand().getTotalTempEggs();
        int current = bird.getNest().getNumberOfEggs() + player.getHand().getTempEggsForBird(bird);

        if (eggsRemaining <= 0 || current >= bird.getNest().getCapacity()) {
            event.reply("Cannot add more eggs to this bird").setEphemeral(true).queue();
            return;
        }

        player.getHand().addTempEgg(bird);

        DiscordMessage msg = buildLayEggsBirdMessage(game, player, habitatEnum, maxEggs);
        event.editMessage(msg.content())
                .setComponents(msg.components())
                .queue();
    }

    private static void layEggsRemoveBird(ButtonInteractionEvent event, Game game, Player player) {
        String buttonComponentId = event.getComponentId().split(":")[0];
        int birdIndex = resolveBirdIndex(buttonComponentId);
        HabitatEnum habitatEnum = parseCurrentHabitatFromMessage(event.getMessage().getContentRaw());
        if (habitatEnum == null) return;

        List<BirdCard> birds = player.getBoard().getHabitat(habitatEnum).getBirds();
        if (birdIndex >= birds.size()) return;

        BirdCard bird = birds.get(birdIndex);
        if (player.getHand().getTempEggsForBird(bird) <= 0) {
            event.reply("No eggs to remove from this bird").setEphemeral(true).queue();
            return;
        }

        player.getHand().removeTempEgg(bird);

        int maxEggs = parseMaxEggsFromMessage(event.getMessage().getContentRaw());
        DiscordMessage msg = buildLayEggsBirdMessage(game, player, habitatEnum, maxEggs);
        event.editMessage(msg.content())
                .setComponents(msg.components())
                .queue();
    }

    private static void layEggsBackToHabitat(ButtonInteractionEvent event, Game game, Player player) {
        int maxEggs = parseMaxEggsFromMessage(event.getMessage().getContentRaw());
        DiscordMessage msg = buildLayEggsHabitatMessage(game, player, maxEggs);
        event.editMessage(msg.content())
                .setComponents(msg.components())
                .queue();
    }

    private static void submitLayEggs(ButtonInteractionEvent event, Game game, Player player) {
        int totalEggs = player.getHand().getTotalTempEggs();
        player.getHand().confirmLayEggs();

        event.editMessage(Constants.PICK_ACTION + BoardAction.LAY_EGGS.getLabel() + "\n\n" +
                        EmojiEnum.EGG.getEmoteId() + " Laid " + totalEggs + " egg" + (totalEggs != 1 ? "s" : ""))
                .setComponents()
                .queue();

        GameService.getInstance().confirmLayEggs(game, player, totalEggs);
    }

    // ======================== DRAW CARDS ========================

    public static DiscordMessage buildDrawCardsMessage(Game game, Player player, int maxDraw, int selectedCount) {
        List<Button> drawTrayButtons = new ArrayList<>();
        for (int i = 0; i < DiscordObject.DRAW_FROM_TRAY_IDS.length; i++) {
            drawTrayButtons.add(Button.secondary(DiscordObject.DRAW_FROM_TRAY_IDS[i] + ":" + game.getGameId(), game.getBirdDeck().getTray()[i].getName())
                    .withEmoji(Emoji.fromFormatted(EmojiEnum.BIRD.getEmoteId())));
        }

        return buildDrawCardsMessage(game, player, maxDraw, selectedCount, drawTrayButtons);
    }

    static DiscordMessage buildDrawCardsMessage(Game game, Player player, int maxDraw, int selectedCount, List<Button> drawTrayButtons) {
        Button drawDeckButton = Button.primary(DiscordObject.TAKE_TURN_ACTION_CHOICE_DRAW_CARDS_DRAW_DECK + ":" + game.getGameId(), "Deck")
                .withEmoji(Emoji.fromFormatted(EmojiEnum.CARD.getEmoteId()))
                .withDisabled(selectedCount >= maxDraw);

        String drawnBirds = player.getHand().getTempDrawnBirds().isEmpty() ?
                "Drawn: (nothing yet)" :
                "Drawn: " + player.getHand().getTempDrawnBirds().stream()
                        .map(Card::getName)
                        .collect(Collectors.joining(" | ")) + "\n";
        String message = Constants.PICK_ACTION + BoardAction.DRAW_CARDS.getLabel() + "\n\n" +
                Constants.DRAW_CARDS + "Draw up to **" + maxDraw + "** cards\n" +
                "Cards remaining: " + (maxDraw - selectedCount) + "\n\n" +
                drawnBirds;

        int numberOfResourcesToDiscard = player.getBoard().getWetland().getNumberOfResourcesToDiscard();
        int numberOfResourcesDiscarded = player.getBoard().getWetland().getNumberOfResourcesDiscarded();
        logger.ridiculous(player.getUser().getName() + ": resources to discard = " + numberOfResourcesToDiscard + ", numberOfResourcesDiscarded = " + numberOfResourcesDiscarded);
        Button discardEggsButton = Button.secondary(DiscordObject.TAKE_TURN_ACTION_DISCARD_EGGS + ":" + game.getGameId(), Constants.DISCARD_RESOURCES)
                .withDisabled(numberOfResourcesDiscarded >= numberOfResourcesToDiscard);

        Button submitButton = Button.success(DiscordObject.TAKE_TURN_ACTION_CHOICE_DRAW_CARDS_SUBMIT + ":" + game.getGameId(), Constants.SUBMIT_SELECTION);
        List<ActionRow> components = List.of(
                ActionRow.of(Stream.concat(Stream.of(drawDeckButton), drawTrayButtons.stream()).toList()),
                ActionRow.of(discardEggsButton),
                ActionRow.of(submitButton));
        return new DiscordMessage(message, components);
    }

    private static void toggleTrayBirdSelected(ButtonInteractionEvent event, Game game, Player player) {
        String clickedId = event.getComponentId();

        List<ActionRow> oldRows = event.getMessage().getActionRows();
        ActionRow drawTray = oldRows.get(0);

        String messageContent = event.getMessage().getContentRaw();
        int maxDraw = parseMaxDrawMessage(messageContent);

        // Toggle the clicked button
        List<Button> newTrayButtons = new ArrayList<>();
        AtomicInteger selectedCount = new AtomicInteger(player.getHand().getTempDrawnBirds().size());
        drawTray.getComponents().stream()
                .filter(component -> component instanceof Button)
                .map(component -> (Button) component)
                .filter(button -> !DiscordObject.TAKE_TURN_ACTION_CHOICE_DRAW_CARDS_DRAW_DECK.name().equals(Objects.requireNonNull(button.getId()).split(":")[0]))
                .forEach(button -> {
                    boolean isClicked = clickedId.equals(button.getId());
                    ButtonStyle currentStyle = button.getStyle();
                    if (isClicked) {
                        ButtonStyle newStyle = (currentStyle == ButtonStyle.SUCCESS) ? ButtonStyle.SECONDARY : ButtonStyle.SUCCESS;
                        Button newButton = (newStyle == ButtonStyle.SUCCESS)
                                ? Button.success(button.getId(), button.getLabel())
                                : Button.secondary(button.getId(), button.getLabel());
                        if (button.getEmoji() != null) {
                            newButton = newButton.withEmoji(button.getEmoji());
                        }
                        newTrayButtons.add(newButton);
                        if (newStyle == ButtonStyle.SUCCESS) selectedCount.getAndIncrement();
                    } else {
                        newTrayButtons.add(button);
                        if (currentStyle == ButtonStyle.SUCCESS) selectedCount.getAndIncrement();
                    }
                });

        // If at max: disable unselected buttons; otherwise enable all
        List<Button> finalTrayButtons = new ArrayList<>();
        newTrayButtons.stream()
                .filter(button -> !DiscordObject.TAKE_TURN_ACTION_CHOICE_DRAW_CARDS_DRAW_DECK.name().equals(Objects.requireNonNull(button.getId()).split(":")[0]))
                .forEach(button -> finalTrayButtons.add(button.withDisabled(selectedCount.get() >= maxDraw && button.getStyle() == ButtonStyle.SECONDARY)));

        // Build the response
        ButtonInteractionProcessor.DiscordMessage msg = ButtonInteractionProcessor.buildDrawCardsMessage(game, player, maxDraw, selectedCount.get(), finalTrayButtons);
        event.editMessage(msg.content())
                .setComponents(msg.components())
                .queue();
    }

    private static void drawCardFromDeck(ButtonInteractionEvent event, Game game, Player player) {
        ActionRow drawTray = event.getMessage().getActionRows().get(0);

        // Draw card from deck
        player.getHand().getTempDrawnBirds().add(game.getBirdDeck().drawCard());

        int maxDraw = parseMaxDrawMessage(event.getMessage().getContentRaw());

        // Count selected tray birds + drawn deck birds
        int selectedCount = player.getHand().getTempDrawnBirds().size();
        List<Button> trayButtons = new ArrayList<>();
        for (ItemComponent component : drawTray.getComponents()) {
            if (component instanceof Button button) {
                String id = Objects.requireNonNull(button.getId()).split(":")[0];
                if (!DiscordObject.TAKE_TURN_ACTION_CHOICE_DRAW_CARDS_DRAW_DECK.name().equals(id)) {
                    if (button.getStyle() == ButtonStyle.SUCCESS) selectedCount++;
                    trayButtons.add(button);
                }
            }
        }

        // Disable unselected buttons if at max
        int finalSelectedCount = selectedCount;
        List<Button> finalTrayButtons = trayButtons.stream()
                .map(button -> button.withDisabled(finalSelectedCount >= maxDraw && button.getStyle() == ButtonStyle.SECONDARY))
                .toList();

        DiscordMessage msg = buildDrawCardsMessage(game, player, maxDraw, selectedCount, finalTrayButtons);
        event.editMessage(msg.content())
                .setComponents(msg.components())
                .queue();
    }

    private static void submitCardDrawSelection(ButtonInteractionEvent event, Game game, Player player) {
        List<ActionRow> oldRows = event.getMessage().getActionRows();
        ActionRow drawTray = oldRows.get(0);
        List<Integer> selectedTrayIndexes = drawTray.getComponents().stream()
                .filter(component -> component instanceof Button)
                .map(component -> (Button) component)
                .filter(button -> button.getStyle() == ButtonStyle.SUCCESS)
                .map(button -> Objects.requireNonNull(button.getId()).split(":")[0])
                .map(ButtonInteractionProcessor::resolveTrayIndex)
                .toList();

        int drawnCards = game.confirmDrawBirdSelection(player, selectedTrayIndexes);

        String drawnBirds = "Drew **" + drawnCards + "** card" + (drawnCards == 1 ? "" : "s") + "\n";
        String message = Constants.PICK_ACTION + BoardAction.DRAW_CARDS.getLabel() + "\n\n" +
                drawnBirds;
        event.editMessage(message)
                .setComponents()
                .queue();

        GameService.getInstance().confirmDrawCards(game, player, drawnCards);
    }

    static int parseMaxDrawMessage(String content) {
        Matcher matcher = Pattern.compile("Draw up to \\*\\*(\\d+)\\*\\* cards").matcher(content);
        int maxDraw = matcher.find() ? Integer.parseInt(matcher.group(1)) : 1;
        logger.debug("Max draw found in message : " + maxDraw);
        return maxDraw;
    }

    private static int resolveTrayIndex(String buttonComponentId) {
        for (int i = 0; i < DiscordObject.DRAW_FROM_TRAY_IDS.length; i++) {
            if (DiscordObject.DRAW_FROM_TRAY_IDS[i].name().equals(buttonComponentId)) {
                return i;
            }
        }
        return 0;
    }

    private static void discardEggToDrawCard(ButtonInteractionEvent event, Game game, Player player) {
        // Build select options for all birds with eggs across all habitats
        List<SelectOption> options = new ArrayList<>();
        for (HabitatEnum habitat : HabitatEnum.values()) {
            for (BirdCard bird : player.getBoard().getHabitat(habitat).getBirds()) {
                int eggs = bird.getNest().getNumberOfEggs();
                if (eggs > 0) {
                    options.add(SelectOption.of(bird.getName(), bird.getName())
                            .withDescription(habitat.getJsonValue() + " | " + eggs + " egg" + (eggs != 1 ? "s" : "")));
                }
            }
        }

        if (options.isEmpty()) {
            event.reply("You don't have any birds with eggs to discard").setEphemeral(true).queue();
            return;
        }

        StringSelectMenu selectMenu = StringSelectMenu.create(DiscordObject.TAKE_TURN_ACTION_CHOICE_DISCARD_EGGS_BIRD_SELECT_MENU.name() + ":" + game.getGameId())
                .setPlaceholder("Pick a bird to remove an egg from")
                .setMinValues(1)
                .setMaxValues(1)
                .addOptions(options)
                .build();

        // Replace the discard button row with the select menu, keep other rows
        List<ActionRow> oldRows = event.getMessage().getActionRows();
        List<ActionRow> newRows = List.of(
                oldRows.get(0),              // draw tray/deck row
                ActionRow.of(selectMenu),    // replace discard button
                oldRows.get(2)               // submit row
        );

        event.editMessage(event.getMessage().getContentRaw())
                .setComponents(newRows)
                .queue();
    }

    // ======================== LOBBY HANDLERS ========================

    private static void handleLobbyButton(ButtonInteractionEvent event) {
        Optional<DiscordBotService.LobbyContext> lobbyContextOptional = DiscordBotService.resolveLobbyContext(event);
        if (lobbyContextOptional.isEmpty()) return;
        DiscordBotService.LobbyContext ctx = lobbyContextOptional.get();
        GameLobby lobby = ctx.lobby();

        switch (DiscordObject.valueOf(ctx.componentId())) {
            // Config phase (creator only)
            case CREATE_GAME_BOARD_TYPE_BUTTON -> withCreatorCheck(event, lobby, () -> toggleBoardType(event, lobby));
            case CREATE_GAME_TEST_DATA_BUTTON -> withCreatorCheck(event, lobby, () -> toggleTestData(event, lobby));
            case CREATE_GAME_SET_SEED_BUTTON -> withCreatorCheck(event, lobby, () -> openSeedModal(event, lobby));
            case CREATE_GAME_PLAYER_COUNT_DECREMENT -> withCreatorCheck(event, lobby, () -> changePlayerCount(event, lobby, -1));
            case CREATE_GAME_PLAYER_COUNT_INCREMENT -> withCreatorCheck(event, lobby, () -> changePlayerCount(event, lobby, 1));
            case CREATE_GAME_START_BUTTON -> withCreatorCheck(event, lobby, () -> createLobby(event, lobby));
            // Waiting phase (anyone)
            case CREATE_GAME_JOIN_BUTTON -> joinGame(event, lobby);
            case CREATE_GAME_LEAVE_BUTTON -> leaveGame(event, lobby);
            default -> logger.warn("Unmatched lobby button: " + ctx.componentId());
        }
    }

    private static void withCreatorCheck(ButtonInteractionEvent event, GameLobby lobby, Runnable action) {
        if (event.getUser().getIdLong() != lobby.getCreator().getIdLong()) {
            event.reply("Only the game creator can change settings.").setEphemeral(true).queue();
            return;
        }
        action.run();
    }

    private static void toggleBoardType(ButtonInteractionEvent event, GameLobby lobby) {
        lobby.setNectarBoard(!lobby.isNectarBoard());
        reRenderLobby(event, lobby);
    }

    private static void toggleTestData(ButtonInteractionEvent event, GameLobby lobby) {
        lobby.setTestData(!lobby.isTestData());
        reRenderLobby(event, lobby);
    }

    private static void openSeedModal(ButtonInteractionEvent event, GameLobby lobby) {
        TextInput seedInput = TextInput.create("seed_value", "Seed (number, or leave blank for random)", TextInputStyle.SHORT)
                .setRequired(false)
                .setPlaceholder("e.g. 12345")
                .build();

        Modal modal = Modal.create(DiscordObject.CREATE_GAME_SET_SEED_BUTTON.name() + ":" + lobby.getLobbyId(), "Set Game Seed")
                .addActionRow(seedInput)
                .build();

        event.replyModal(modal).queue();
    }

    private static void changePlayerCount(ButtonInteractionEvent event, GameLobby lobby, int delta) {
        int newCount = lobby.getPlayerCount() + delta;
        if (newCount < 1 || newCount > Constants.LOBBY_MAX_PLAYERS) {
            event.deferEdit().queue();
            return;
        }
        lobby.setPlayerCount(newCount);
        reRenderLobby(event, lobby);
    }

    private static void createLobby(ButtonInteractionEvent event, GameLobby lobby) {
        if (lobby.getPlayerCount() == 1) {
            // Solo game: start immediately
            launchGame(event, lobby);
            return;
        }
        lobby.setWaitingForPlayers(true);
        reRenderLobby(event, lobby);
    }

    private static void joinGame(ButtonInteractionEvent event, GameLobby lobby) {
        User user = event.getUser();
        if (user.isBot()) {
            event.reply("Bots can't play Wingspan!").setEphemeral(true).queue();
            return;
        }
        if (lobby.getPlayers().stream().anyMatch(p -> p.getIdLong() == user.getIdLong())) {
            event.reply("You have already joined this game.").setEphemeral(true).queue();
            return;
        }
        if (lobby.getPlayers().size() >= lobby.getPlayerCount()) {
            event.reply("This game is full.").setEphemeral(true).queue();
            return;
        }
        lobby.getPlayers().add(user);

        // Auto-start when full
        if (lobby.getPlayers().size() >= lobby.getPlayerCount()) {
            launchGame(event, lobby);
            return;
        }
        reRenderLobby(event, lobby);
    }

    private static void leaveGame(ButtonInteractionEvent event, GameLobby lobby) {
        User user = event.getUser();
        if (user.getIdLong() == lobby.getCreator().getIdLong()) {
            event.reply("The game creator cannot leave.").setEphemeral(true).queue();
            return;
        }
        boolean removed = lobby.getPlayers().removeIf(p -> p.getIdLong() == user.getIdLong());
        if (!removed) {
            event.reply("You are not in this game.").setEphemeral(true).queue();
            return;
        }
        reRenderLobby(event, lobby);
    }

    private static void launchGame(ButtonInteractionEvent event, GameLobby lobby) {
        boolean testData = lobby.isTestData();
        Game game = GameService.getInstance().createGameFromLobby(lobby);
        String gameId = game.getGameId();

        if (testData) {
            game.getPlayers().forEach(player -> CreateGame.addTestData(game, player));
        }

        Button pickHandButton = Button.success(DiscordObject.PROMPT_PICK_HAND_BUTTON.name() + ":" + gameId, "\uD83D\uDC50 Pick Starting Hand");
        Button seeFeederButton = Button.secondary(DiscordObject.PROMPT_SEE_FEEDER_BUTTON.name() + ":" + gameId, "\uD83C\uDFB2 See Feeder");
        Button seeTrayButton = Button.secondary(DiscordObject.PROMPT_SEE_TRAY_BUTTON.name() + ":" + gameId, "\uD83D\uDC26 See Tray");

        String playersAsMention = game.getPlayers().stream()
                .map(p -> p.getUser().getAsMention())
                .collect(Collectors.joining(" "));

        event.editMessage("Game `" + gameId + "` created with " + playersAsMention + "\n")
                .setEmbeds()
                .setComponents(ActionRow.of(pickHandButton, seeFeederButton, seeTrayButton))
                .queue();
    }

    private static void reRenderLobby(ButtonInteractionEvent event, GameLobby lobby) {
        event.editMessageEmbeds(CreateGame.buildLobbyEmbed(lobby))
                .setComponents(CreateGame.buildLobbyComponents(lobby))
                .queue();
    }

}
