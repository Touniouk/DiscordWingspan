package test;

import game.Game;
import game.components.Feeder;
import game.components.subcomponents.Die;
import game.components.subcomponents.DieFace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeederTest {

    private Feeder feeder;

    @BeforeEach
    void setUp() {
        Game.GAME_SEED = 42L;
        feeder = new Feeder(false);
    }

    /**
     * Sets the visible face of a die via reflection.
     */
    private static void setVisibleFace(Die die, DieFace face) throws Exception {
        Field field = Die.class.getDeclaredField("visibleFace");
        field.setAccessible(true);
        field.set(die, face);
    }

    // ===== canBeRerolled =====

    @Test
    void testCanBeRerolled_allSameFace_returnsTrue() throws Exception {
        List<Die> dice = feeder.getDiceInFeeder();
        for (Die die : dice) {
            setVisibleFace(die, DieFace.WORM);
        }
        assertTrue(feeder.canBeRerolled());
    }

    @Test
    void testCanBeRerolled_allSameFace_differentType_returnsTrue() throws Exception {
        List<Die> dice = feeder.getDiceInFeeder();
        for (Die die : dice) {
            setVisibleFace(die, DieFace.FISH);
        }
        assertTrue(feeder.canBeRerolled());
    }

    @Test
    void testCanBeRerolled_allDualFoodFace_returnsTrue() throws Exception {
        List<Die> dice = feeder.getDiceInFeeder();
        for (Die die : dice) {
            setVisibleFace(die, DieFace.WORM_SEED);
        }
        assertTrue(feeder.canBeRerolled());
    }

    @Test
    void testCanBeRerolled_twoDifferentFaces_returnsFalse() throws Exception {
        List<Die> dice = feeder.getDiceInFeeder();
        setVisibleFace(dice.get(0), DieFace.WORM);
        for (int i = 1; i < dice.size(); i++) {
            setVisibleFace(dice.get(i), DieFace.SEED);
        }
        assertFalse(feeder.canBeRerolled());
    }

    @Test
    void testCanBeRerolled_mixOfMultipleFaces_returnsFalse() throws Exception {
        List<Die> dice = feeder.getDiceInFeeder();
        setVisibleFace(dice.get(0), DieFace.WORM);
        setVisibleFace(dice.get(1), DieFace.SEED);
        setVisibleFace(dice.get(2), DieFace.FISH);
        setVisibleFace(dice.get(3), DieFace.FRUIT);
        setVisibleFace(dice.get(4), DieFace.MOUSE);
        assertFalse(feeder.canBeRerolled());
    }

    @Test
    void testCanBeRerolled_emptyFeeder_returnsTrue() {
        // Remove all dice from feeder — getDice triggers reroll when empty,
        // so remove them one at a time leaving one, then remove the last
        while (feeder.getDiceInFeeder().size() > 1) {
            feeder.getDiceInFeeder().remove(0);
        }
        feeder.getDiceInFeeder().remove(0);
        assertTrue(feeder.canBeRerolled());
    }

    @Test
    void testCanBeRerolled_singleDie_returnsTrue() throws Exception {
        // Reduce to a single die
        while (feeder.getDiceInFeeder().size() > 1) {
            feeder.getDiceInFeeder().remove(0);
        }
        setVisibleFace(feeder.getDiceInFeeder().get(0), DieFace.FRUIT);
        assertTrue(feeder.canBeRerolled());
    }

    @Test
    void testCanBeRerolled_twoDice_sameFace_returnsTrue() throws Exception {
        while (feeder.getDiceInFeeder().size() > 2) {
            feeder.getDiceInFeeder().remove(0);
        }
        List<Die> dice = feeder.getDiceInFeeder();
        setVisibleFace(dice.get(0), DieFace.MOUSE);
        setVisibleFace(dice.get(1), DieFace.MOUSE);
        assertTrue(feeder.canBeRerolled());
    }

    @Test
    void testCanBeRerolled_twoDice_differentFaces_returnsFalse() throws Exception {
        while (feeder.getDiceInFeeder().size() > 2) {
            feeder.getDiceInFeeder().remove(0);
        }
        List<Die> dice = feeder.getDiceInFeeder();
        setVisibleFace(dice.get(0), DieFace.MOUSE);
        setVisibleFace(dice.get(1), DieFace.FISH);
        assertFalse(feeder.canBeRerolled());
    }

    @Test
    void testCanBeRerolled_fourSameOneDifferent_returnsFalse() throws Exception {
        List<Die> dice = feeder.getDiceInFeeder();
        for (int i = 0; i < 4; i++) {
            setVisibleFace(dice.get(i), DieFace.SEED);
        }
        setVisibleFace(dice.get(4), DieFace.WORM);
        assertFalse(feeder.canBeRerolled());
    }

    @Test
    void testCanBeRerolled_afterRemovingDice_allRemainingSame_returnsTrue() throws Exception {
        List<Die> dice = feeder.getDiceInFeeder();
        // Set all to different faces first
        setVisibleFace(dice.get(0), DieFace.WORM);
        setVisibleFace(dice.get(1), DieFace.SEED);
        setVisibleFace(dice.get(2), DieFace.SEED);
        setVisibleFace(dice.get(3), DieFace.SEED);
        setVisibleFace(dice.get(4), DieFace.SEED);
        assertFalse(feeder.canBeRerolled());

        // Remove the odd one out
        feeder.getDiceInFeeder().remove(0);
        assertTrue(feeder.canBeRerolled());
    }
}
