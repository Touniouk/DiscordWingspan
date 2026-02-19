package game;

import game.components.Board;
import game.components.Hand;
import game.components.NectarBoard;
import game.components.enums.FoodType;
import game.components.subcomponents.BirdCard;
import game.components.subcomponents.BonusCard;
import game.components.subcomponents.Card;
import game.exception.GameInputException;
import game.service.enumeration.PlayerState;
import game.service.enumeration.PlayerStateMachine;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.User;
import util.LogLevel;
import util.Logger;

import java.util.List;
import java.util.Map;

/**
 * Represents a player in the game, holding their Discord user, board, hand, and current state.
 */
@Getter
@Setter
public class Player {
    private final User user;
    private final Board board;
    private final Hand hand;

    private PlayerState state;
    private long turnPromptMessageId;
    private String lastActionAsString;

    private final Logger logger = new Logger(Player.class, LogLevel.ALL);

    /**
     * @param user       the Discord user this player represents
     * @param withNectar whether to use the Oceania nectar board variant
     */
    public Player(User user, boolean withNectar) {
        this.state = PlayerState.WAITING_FOR_STARTING_HAND;
        this.user = user;
        this.board = withNectar  ? new NectarBoard() : new Board();
        this.hand = new Hand(withNectar);
        logger.unnecessary(user.getName() + " set up");
    }

    public void addBirdInHand(BirdCard birdCard) {
        this.hand.addBird(birdCard);
    }

    public void addBonusInHand(BonusCard bonusCard) {
        this.hand.addBonus(bonusCard);
    }

    /**
     * Validates and confirms the player's starting hand selection.
     * Ensures the player hasn't selected more than 5 birds + food or more than 1 bonus card,
     * then transitions the player to the READY state.
     *
     * @throws GameInputException if too many resources or bonus cards are selected
     */
    public void confirmStartingHandPick() throws GameInputException {
        // Check that we didn't select too much
        int foodSelected = hand.getPantry().values().stream().mapToInt(Integer::intValue).sum();
        int birdCardsSelected = (int) hand.getBirdCards().stream().filter(Card::isSelected).count();
        int bonusCardsSelected = (int) hand.getBonusCards().stream().filter(Card::isSelected).count();
        if (foodSelected + birdCardsSelected > 5) {
            throw new GameInputException("You selected too many resources (" + (foodSelected + birdCardsSelected) + ")");
        } else if (hand.getBonusCards().stream().filter(Card::isSelected).count() > 1) {
            throw new GameInputException("You selected too many bonus cards (" + bonusCardsSelected + ")");
        }

        PlayerStateMachine.transition(this, PlayerState.READY);
    }

    public void automaticallySelectFood(BirdCard birdToPlay) {
        if (birdToPlay.getFoodCost().size() == 1) {
            List<FoodType> foodCost = birdToPlay.getFoodCost().get(0);
            int numberOfWilds = 0;
            for (FoodType food : foodCost) {
                if (FoodType.WILD == food) {
                    numberOfWilds++;
                } else if (this.getHand().getTempPantryAvailableFood().get(food) > 0) {
                    this.getHand().getTempPantrySpentFood().merge(food, 1, Integer::sum);
                }
            }
            for (Map.Entry<FoodType, Integer> entry : this.getHand().getTempPantryAvailableFood().entrySet()) {
                if (numberOfWilds <= 0) break;
                if (entry.getValue() > 0) {
                    this.getHand().getTempPantrySpentFood().merge(entry.getKey(), 1, Integer::sum);
                    numberOfWilds--;
                }
            }
            // TODO: check nectar?
        }
        this.getHand().getTempPantrySpentFood().forEach((k, v) -> {
            this.getHand().getTempPantryAvailableFood().merge(k, v, (acc, i) -> acc - i);
        });
    }
}
