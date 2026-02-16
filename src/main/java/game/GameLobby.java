package game;

import game.components.enums.Expansion;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GameLobby {
    private final String lobbyId;
    private final User creator;
    private final TextChannel gameChannel;
    private final List<User> players = new ArrayList<>();
    private List<Expansion> expansions = new ArrayList<>(List.of(Expansion.values()));
    private boolean nectarBoard = false;
    private boolean testData = false;
    private long seed = 0; // 0 = random
    private int playerCount = 2;
    private boolean waitingForPlayers = false;

    public GameLobby(String lobbyId, User creator, TextChannel gameChannel) {
        this.lobbyId = lobbyId;
        this.creator = creator;
        this.gameChannel = gameChannel;
        this.players.add(creator);
    }
}
