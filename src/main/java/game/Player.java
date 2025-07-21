package game;

import game.components.Board;
import game.components.Hand;
import game.components.NectarBoard;
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

@Getter
@Setter
public class Player {
    private final User user;
    private final Board board;
    private final Hand hand;

    private PlayerState state;

    private final Logger logger = new Logger(Player.class, LogLevel.ALL);

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
}
