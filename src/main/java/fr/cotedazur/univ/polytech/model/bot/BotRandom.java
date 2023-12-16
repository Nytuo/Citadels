package fr.cotedazur.univ.polytech.model.bot;

import java.util.List;
import java.util.Random;

import fr.cotedazur.univ.polytech.model.card.CharacterCard;
import fr.cotedazur.univ.polytech.model.card.DistrictCard;
import fr.cotedazur.univ.polytech.model.deck.DistrictDeck;

public class BotRandom extends Player implements GameActions {

    private final Random random = new Random();

    public BotRandom() {
        super();
    }

    @Override
    public String startChoice(DistrictDeck districtDeck) {
        int randomIndex = random.nextInt(2);
        switch (randomIndex) {
            case 0 -> {
                return collectTwoGolds();
            }
            case 1 -> {
               return drawCard(districtDeck);
            }
            default -> {
                return null;
            }
        }
    }

    @Override
    public String choiceToPutADistrict() {
        int randomIndex = random.nextInt(2);
        if (randomIndex == 0) {
            return putADistrict();
        }
        return null;
    }

    @Override
    public String putADistrict() {
        if (!getHands().isEmpty()) {
            int randomIndex = random.nextInt(getHands().size());
            DistrictCard card = getHands().get(randomIndex);

            return card.name();
        }
        return null;
    }

    @Override
    public void useRoleEffect() {
        // TODO when the bot will be able to use the effects of its character
    }

    @Override
    public int chooseCharacter(List<CharacterCard> cards) {
        return random.nextInt(cards.size()); //return a random number between 0 and the size of the list
    }
}
