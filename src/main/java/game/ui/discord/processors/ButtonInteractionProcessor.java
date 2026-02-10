package game.ui.discord.processors;

import game.Game;
import game.Player;
import game.components.enums.FoodType;
import game.components.subcomponents.*;
import game.exception.GameInputException;
import game.service.DiscordBotService;
import game.service.GameService;
import game.ui.discord.commands.SeeBoard;
import game.ui.discord.commands.TakeTurn;
import game.ui.discord.enumeration.Constants;
import game.ui.discord.enumeration.DiscordObject;
import game.ui.discord.enumeration.EmojiEnum;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import util.LogLevel;
import util.Logger;
import util.StringUtil;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class ButtonInteractionProcessor {

    private static final Logger logger = new Logger(ButtonInteractionProcessor.class, LogLevel.ALL);

    public static void handleCommand(ButtonInteractionEvent event) {
        String[] arr = event.getComponentId().split(":");
        String componentId = arr[0];
        String gameId = arr[1];
        long userId = event.getUser().getIdLong();
        Game currentGame;
        Player currentPlayer;
        try {
            currentGame = DiscordBotService.getInstance().getGameFromId(event, gameId);
            currentPlayer = currentGame.getPlayerById(event.getUser().getIdLong());
        } catch (GameInputException ex) {
            event.reply(ex.getMessage()).setEphemeral(true).queue();
            return;
        }

        try {
            switch (DiscordObject.valueOf(componentId)) {
                case PICK_STARTING_HAND_SUBMIT_BUTTON -> pickStartingHandSubmitButton(event, currentGame, userId);
                case PICK_STARTING_HAND_RANDOMISE_BUTTON -> pickStartingHandRandomiseButton(event, currentGame, currentPlayer);
                case TAKE_TURN_ACTION_CHOICE_PLAY_BIRD_CHOOSE_FOOD_ADD_WORM -> takeTurnActionChoicePlayBirdChooseFoodAddFood(event, FoodType.WORM, currentPlayer);
                case TAKE_TURN_ACTION_CHOICE_PLAY_BIRD_CHOOSE_FOOD_REMOVE_WORM -> takeTurnActionChoicePlayBirdChooseFoodRemoveFood(event, FoodType.WORM, currentPlayer);
                case TAKE_TURN_ACTION_CHOICE_PLAY_BIRD_CHOOSE_FOOD_ADD_SEED -> takeTurnActionChoicePlayBirdChooseFoodAddFood(event, FoodType.SEED, currentPlayer);
                case TAKE_TURN_ACTION_CHOICE_PLAY_BIRD_CHOOSE_FOOD_REMOVE_SEED -> takeTurnActionChoicePlayBirdChooseFoodRemoveFood(event, FoodType.SEED, currentPlayer);
                case TAKE_TURN_ACTION_CHOICE_PLAY_BIRD_CHOOSE_FOOD_ADD_FRUIT -> takeTurnActionChoicePlayBirdChooseFoodAddFood(event, FoodType.FRUIT, currentPlayer);
                case TAKE_TURN_ACTION_CHOICE_PLAY_BIRD_CHOOSE_FOOD_REMOVE_FRUIT -> takeTurnActionChoicePlayBirdChooseFoodRemoveFood(event, FoodType.FRUIT, currentPlayer);
                case TAKE_TURN_ACTION_CHOICE_PLAY_BIRD_CHOOSE_FOOD_ADD_FISH -> takeTurnActionChoicePlayBirdChooseFoodAddFood(event, FoodType.FISH, currentPlayer);
                case TAKE_TURN_ACTION_CHOICE_PLAY_BIRD_CHOOSE_FOOD_REMOVE_FISH -> takeTurnActionChoicePlayBirdChooseFoodRemoveFood(event, FoodType.FISH, currentPlayer);
                case TAKE_TURN_ACTION_CHOICE_PLAY_BIRD_CHOOSE_FOOD_ADD_RODENT -> takeTurnActionChoicePlayBirdChooseFoodAddFood(event, FoodType.RODENT, currentPlayer);
                case TAKE_TURN_ACTION_CHOICE_PLAY_BIRD_CHOOSE_FOOD_REMOVE_RODENT -> takeTurnActionChoicePlayBirdChooseFoodRemoveFood(event, FoodType.RODENT, currentPlayer);
                case TAKE_TURN_ACTION_CHOICE_PLAY_BIRD_CHOOSE_FOOD_ADD_NECTAR -> takeTurnActionChoicePlayBirdChooseFoodAddFood(event, FoodType.NECTAR, currentPlayer);
                case TAKE_TURN_ACTION_CHOICE_PLAY_BIRD_CHOOSE_FOOD_REMOVE_NECTAR -> takeTurnActionChoicePlayBirdChooseFoodRemoveFood(event, FoodType.NECTAR, currentPlayer);
                case TAKE_TURN_ACTION_CHOICE_PLAY_BIRD_CHOOSE_FOOD_SUBMIT_BUTTON -> takeTurnActionChoicePlayBirdChooseFoodSubmitButton(event, gameId, currentPlayer);
                case TAKE_TURN_ACTION_CHOICE_GAIN_FOOD_DIE_0,
                     TAKE_TURN_ACTION_CHOICE_GAIN_FOOD_DIE_1,
                     TAKE_TURN_ACTION_CHOICE_GAIN_FOOD_DIE_2,
                     TAKE_TURN_ACTION_CHOICE_GAIN_FOOD_DIE_3,
                     TAKE_TURN_ACTION_CHOICE_GAIN_FOOD_DIE_4 -> toggleGainFoodDie(event, currentGame);
                case TAKE_TURN_ACTION_CHOICE_GAIN_FOOD_SUBMIT_BUTTON -> submitGainFood(event, currentGame, currentPlayer);
                case TAKE_TURN_ACTION_CHOICE_GAIN_FOOD_REROLL_BUTTON -> rerollFeeder(event, currentGame, currentPlayer);
                case PROMPT_TAKE_TURN_BUTTON -> TakeTurn.takeTurn(event, currentGame, currentPlayer);
                case PROMPT_SEE_BOARD_BUTTON -> SeeBoard.seeBoard(event, currentPlayer);
                default -> logger.warn("Button id not matched: " + componentId);
            }
        } catch (GameInputException ex) {
            event.reply(ex.getMessage()).setEphemeral(true).queue();
        }
    }

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

    private static void takeTurnActionChoicePlayBirdChooseFoodRemoveFood(ButtonInteractionEvent event, FoodType foodType, Player currentPlayer) throws GameInputException {
        int numberOfFoodToSpend = currentPlayer.getHand().getTempPantrySpentFood().get(foodType);
        int numberOfFoodWeHave = currentPlayer.getHand().getTempPantryAvailableFood().get(foodType);
        if (numberOfFoodToSpend <= 0) {
            throw new GameInputException("You cannot use a negative amount of food");
        }
        currentPlayer.getHand().getTempPantrySpentFood().put(foodType, numberOfFoodToSpend - 1);
        currentPlayer.getHand().getTempPantryAvailableFood().put(foodType, numberOfFoodWeHave + 1);

        showFoodUsedMessage(event, currentPlayer);
    }

    private static void takeTurnActionChoicePlayBirdChooseFoodAddFood(ButtonInteractionEvent event, FoodType foodType, Player currentPlayer) throws GameInputException {
        int numberOfFoodToSpend = currentPlayer.getHand().getTempPantrySpentFood().get(foodType);
        int numberOfFoodWeHave = currentPlayer.getHand().getTempPantryAvailableFood().get(foodType);
        if (numberOfFoodWeHave <= 0) {
            throw new GameInputException("You've used all your " + foodType.getDisplayName());
        }
        currentPlayer.getHand().getTempPantrySpentFood().put(foodType, numberOfFoodToSpend + 1);
        currentPlayer.getHand().getTempPantryAvailableFood().put(foodType, numberOfFoodWeHave - 1);

        showFoodUsedMessage(event, currentPlayer);
    }

    private static void showFoodUsedMessage(ButtonInteractionEvent event, Player currentPlayer) {
        String[] message = event.getMessage().getContentRaw().split("\n\n");
        event.editMessage(message[0] + "\n\n" +
                        message[1] + "\n\n" +
                        Constants.CHOOSE_FOOD_TO_USE + "\n" +
                        "Food used: " + EmojiEnum.getFoodAsEmojiList(currentPlayer.getHand().getTempPantrySpentFood()) + "\n" +
                        "Food in hand: " + EmojiEnum.getFoodAsEmojiList(currentPlayer.getHand().getTempPantryAvailableFood()))
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
                                    .withEmoji(Emoji.fromFormatted(EmojiEnum.getEmoteIdFromFoodType(foods.get(0))));
                        } else if (button.getLabel().startsWith(foods.get(0).getDisplayName())) {
                            // Food 0 selected → select food 1
                            newButton = Button.success(button.getId(), foods.get(1).getDisplayName() + " (2/2)")
                                    .withEmoji(Emoji.fromFormatted(EmojiEnum.getEmoteIdFromFoodType(foods.get(1))));
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

    static FeedPickerMessage buildFeedPickerMessage(Game game, int maxFood, String foodGainedSoFar) {
        List<Die> dice = game.getFeeder().getDiceInFeeder();
        String gameId = game.getGameId();

        List<Button> dieButtons = new ArrayList<>();
        for (int i = 0; i < dice.size(); i++) {
            DieFace face = dice.get(i).getVisibleFace();
            dieButtons.add(Button.secondary(GAIN_FOOD_DIE_IDS[i].name() + ":" + gameId, face.getLabel())
                    .withEmoji(Emoji.fromFormatted(EmojiEnum.getFirstEmojiFromDieFace(face))));
        }

        Button submitButton = Button.success(DiscordObject.TAKE_TURN_ACTION_CHOICE_GAIN_FOOD_SUBMIT_BUTTON.name() + ":" + gameId, Constants.SUBMIT_SELECTION);
        Button rerollButton = game.getFeeder().canBeRerolled() ?
                Button.primary(DiscordObject.TAKE_TURN_ACTION_CHOICE_GAIN_FOOD_REROLL_BUTTON.name() + ":" + gameId, "\uD83C\uDFB2 Reroll Feeder") :
                Button.primary(DiscordObject.TAKE_TURN_ACTION_CHOICE_GAIN_FOOD_REROLL_BUTTON.name() + ":" + gameId, "\uD83C\uDFB2 Reroll Feeder").asDisabled();

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
        return new FeedPickerMessage(message, rows);
    }

    record FeedPickerMessage(String content, List<ActionRow> components) {}

    private static int resolveDieIndex(String buttonComponentId) {
        for (int i = 0; i < GAIN_FOOD_DIE_IDS.length; i++) {
            if (GAIN_FOOD_DIE_IDS[i].name().equals(buttonComponentId)) {
                return i;
            }
        }
        return 0;
    }

    private static final DiscordObject[] GAIN_FOOD_DIE_IDS = {
            DiscordObject.TAKE_TURN_ACTION_CHOICE_GAIN_FOOD_DIE_0,
            DiscordObject.TAKE_TURN_ACTION_CHOICE_GAIN_FOOD_DIE_1,
            DiscordObject.TAKE_TURN_ACTION_CHOICE_GAIN_FOOD_DIE_2,
            DiscordObject.TAKE_TURN_ACTION_CHOICE_GAIN_FOOD_DIE_3,
            DiscordObject.TAKE_TURN_ACTION_CHOICE_GAIN_FOOD_DIE_4
    };

    private static List<Integer> getSelectedDice(ButtonInteractionEvent event) {
        ActionRow dieRow = event.getMessage().getActionRows().get(0);

        // Find which die indices are selected (SUCCESS style)
        List<Integer> selectedIndices = new ArrayList<>();
        for (ItemComponent component : dieRow.getComponents()) {
            if (component instanceof Button button && button.getStyle() == ButtonStyle.SUCCESS) {
                String buttonComponentId = Objects.requireNonNull(button.getId()).split(":")[0];
                for (int i = 0; i < GAIN_FOOD_DIE_IDS.length; i++) {
                    if (GAIN_FOOD_DIE_IDS[i].name().equals(buttonComponentId)) {
                        selectedIndices.add(i);
                        break;
                    }
                }
            }
        }
        return selectedIndices;
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
            foodGained.append(EmojiEnum.getEmoteIdFromFoodType(foodType)).append(" ");
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

        FeedPickerMessage picker = buildFeedPickerMessage(currentGame, maxFood, allFood);
        event.editMessage(picker.content())
                .setComponents(picker.components())
                .queue();
    }
}
