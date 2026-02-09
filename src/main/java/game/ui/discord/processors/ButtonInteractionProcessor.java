package game.ui.discord.processors;

import game.Game;
import game.Player;
import game.components.enums.FoodType;
import game.components.subcomponents.BirdCard;
import game.components.subcomponents.BonusCard;
import game.components.subcomponents.Card;
import game.exception.GameInputException;
import game.service.DiscordBotService;
import game.service.GameService;
import game.ui.discord.enumeration.Constants;
import game.ui.discord.enumeration.DiscordObject;
import game.ui.discord.enumeration.EmojiEnum;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import util.LogLevel;
import util.Logger;
import util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
                case TAKE_TURN_ACTION_CHOICE_PLAY_BIRD_CHOOSE_FOOD_ADD_WORM -> takeTurnActionChoicePlayBirdChooseFoodAddFood(event, FoodType.INVERTEBRATE, currentPlayer);
                case TAKE_TURN_ACTION_CHOICE_PLAY_BIRD_CHOOSE_FOOD_REMOVE_WORM -> takeTurnActionChoicePlayBirdChooseFoodRemoveFood(event, FoodType.INVERTEBRATE, currentPlayer);
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
            throw new GameInputException("You've used all your " + foodType.getJsonName());
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
                .addField(Constants.FOOD_SELECTED_FIELD, foodSelected.isEmpty() ? "None" : StringUtil.getListAsString(foodSelected.stream().map(FoodType::getJsonName), ", "), true)
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
        event.deferEdit().queue(hook -> {
            hook.editOriginalComponents().queue();
        });
        currentGame.getGameChannel().sendMessage(
                event.getUser().getAsMention() + " confirmed their starting hand"
        ).queue();
        GameService.getInstance().checkAllPlayersReady(currentGame);
    }
}
