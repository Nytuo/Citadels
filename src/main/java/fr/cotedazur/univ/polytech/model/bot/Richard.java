package fr.cotedazur.univ.polytech.model.bot;

import fr.cotedazur.univ.polytech.model.card.CharacterCard;
import fr.cotedazur.univ.polytech.model.card.Color;
import fr.cotedazur.univ.polytech.model.card.DistrictCard;
import fr.cotedazur.univ.polytech.model.card.DistrictCardComparator;

import java.util.*;
import java.util.stream.Collectors;


public class Richard extends Player implements GameActions {

    private Random random = new Random();

    public Richard() {
        super();
    }

    private CharacterCard target;

    @Override
    public DispatchState startChoice() {
        discoverValidCard();
        Set<Color> colorsOnBoard = colorInList(getBoard());
        for (DistrictCard districtCard : validCards){
            if (!colorsOnBoard.contains(districtCard.getDistrictColor())){
                return DispatchState.TWO_GOLDS;

            }
        }
        if (getGolds() <= 4) {
            return DispatchState.TWO_GOLDS;
        }
        if (getHands().isEmpty() || validCards.isEmpty() || getGolds() >= 6) {
            return DispatchState.DRAW_CARD;
        }
        return DispatchState.TWO_GOLDS;
    }


    @Override
    public DistrictCard choiceHowToPlayDuringTheRound() {
        return putADistrict();
    }

    @Override
    public DistrictCard putADistrict() {
        discoverValidCard();
        //List des différentes couleurs sur le Terrain
        Set<Color> colorsOnBoard = colorInList(getBoard());
        if (!validCards.isEmpty()) {
            List<DistrictCard> purpleCard = new ArrayList<>();
            List<DistrictCard> colorNotOnBoard = new ArrayList<>();
            List<DistrictCard> cardsThatMatchWithRoleColor = new ArrayList<>();
            for (DistrictCard districtCard : validCards) {
                if (districtCard.getDistrictColor() == Color.PURPLE) {
                    purpleCard.add(districtCard);
                }
                if (!colorsOnBoard.contains(districtCard.getDistrictColor())) {
                    colorNotOnBoard.add(districtCard);
                }
                if(districtCard.getDistrictColor() == getPlayerRole().getCharacterColor()){
                    cardsThatMatchWithRoleColor.add(districtCard);
                }
            }
            if (!purpleCard.isEmpty()) return maxPrice(purpleCard);

            if (!cardsThatMatchWithRoleColor.isEmpty()) return maxPrice(cardsThatMatchWithRoleColor);

            if (!colorNotOnBoard.isEmpty()) return maxPrice(colorNotOnBoard);

            return maxPrice(validCards);
        }

        return null;
    }

    public Set<Color> colorInList(List<DistrictCard> districtCards) {
        Set<Color> listeUnique = new HashSet<>();
        for (DistrictCard districtCard : districtCards) {
            listeUnique.add(districtCard.getDistrictColor());
        }
        return listeUnique;
    }

    public DistrictCard maxPrice(List<DistrictCard> districtCards) {
        if (!districtCards.isEmpty()) {
            DistrictCard maxValue = districtCards.get(0);
            for (DistrictCard card : districtCards) {
                if (maxValue.getDistrictValue() < card.getDistrictValue()) {
                    maxValue = card;
                }
            }
            return maxValue;
        }
        return null;
    }

    @Override
    public CharacterCard selectWhoWillBeAffectedByThiefEffect(List<Player> players, List<CharacterCard> characterCards) {
        //Avoid aggressive characters and opportunist characaters (warlord, thief, assassin, magician, bishop) and remove the visible discarded cards and the character that has been killed
        List<CharacterCard> characterCardsCopy = new ArrayList<>(characterCards);
        characterCardsCopy.removeIf(element -> (element != CharacterCard.ARCHITECT && element != CharacterCard.KING && element != CharacterCard.MERCHANT) || (getDiscardedCardDuringTheRound().contains(element)) || element == getRoleKilledByAssassin());
        if (!characterCardsCopy.isEmpty()) {
            return characterCardsCopy.get(random.nextInt(characterCardsCopy.size()));
        }
        return characterCards.get(random.nextInt(characterCards.size()));
    }

    @Override
    public CharacterCard selectWhoWillBeAffectedByAssassinEffect(List<Player> players, List<CharacterCard> characterCards) {
        if (target == CharacterCard.ARCHITECT) return target;
        if (target == CharacterCard.MAGICIAN) return target;
        if (isFirst(players) || whatCharacterGotTookByGoodPlayer(players, CharacterCard.WARLORD) || onlyOneWith1GoldDistrict(players)) {
            return CharacterCard.WARLORD;
        }
        if (someoneIsGoingToGetRich(players) || whatCharacterGotTookByGoodPlayer(players, CharacterCard.THIEF)) {
            return CharacterCard.THIEF;
        }
        return characterCards.get(random.nextInt(characterCards.size()));
    }

    public boolean whatCharacterGotTookByGoodPlayer(List<Player> players, CharacterCard card) {
        if (getDiscardedCardDuringTheRound().contains(card)) {
            return false;
        }
        List<Player> playersInOrder = getListCopyPlayers();
        for (Player player : players) {
            if (player.equals(this)) continue;
            if (player.getBoard().size() >= 6) {
                if (playersInOrder.indexOf(player) < playersInOrder.indexOf(this)) {
                    return !getCurrentChoiceOfCharactersCardsDuringTheRound().contains(card);
                } else {
                    return getCurrentChoiceOfCharactersCardsDuringTheRound().contains(card);
                }
            }
        }
        return false;
    }

    public boolean someoneIsGoingToGetRich(List<Player> players) {
        int count = 0;
        boolean someonePoor = false;
        for (Player player : players) {
            if (player.getGolds() >= 4) {
                count++;
            }
            if (player.getGolds() <= 1) {
                if (player.equals(this)) continue;
                someonePoor = true;
            }
        }
        return someonePoor && count >= 2;
    }

    public boolean onlyOneWith1GoldDistrict(List<Player> players) {
        for (Player player : players) {
            if (player.equals(this)) continue;
            for (DistrictCard districtCard : player.getBoard()) {
                if (districtCard.getDistrictValue() == 1) {
                    return false;
                }
            }
        }
        for (DistrictCard districtCard : getBoard()) {
            if (districtCard.getDistrictValue() == 1) {
                return true;
            }
        }
        return false;
    }

    public boolean isBeforeLastRound() {
        for (Player player : getListCopyPlayers()) {
            if (player != this && player.getBoard().size() == 6) {
                return true;
            }
        }
        return false;
    }


    public boolean isFirst(List<Player> players){
        return numberOfDistrictOfFirstPlayer(players) == this.getBoard().size();
    }

    public int numberOfDistrictOfFirstPlayer(List<Player> players){
        int countPlayer = 0;
        int maxCountPlayer = 0;
        for (Player player : players) {
            countPlayer = player.getBoard().size();
            if (countPlayer > maxCountPlayer) maxCountPlayer = countPlayer;

        }
        return maxCountPlayer;
    }

    public List<Player> numberOfDistrictInOrder(List<Player> players){
        List<Player> playersInOrder = players;
        Collections.sort(playersInOrder, Comparator.comparingInt(Player::getNbCardsInHand));
        Collections.reverse(playersInOrder);
        System.out.println(playersInOrder.size());
        System.out.println(playersInOrder);
        return playersInOrder;
    }


    public boolean someoneHasNoCards(List<Player> players) {
        for (Player player : players) {
            if (player.equals(this)) continue;
            if (player.getHands().isEmpty()) {
                return true;
            }
        }
        return false;
    }


    private int countNumberOfSpecifiedColorCard(Color color) {
        int count = 0;
        for (DistrictCard card : getBoard()) {
            if (card.getDistrictColor().getColorName().equals(color.getColorName())) count++;
        }
        for (DistrictCard card : getHands()) {
            if (card.getDistrictColor().getColorName().equals(color.getColorName())) {
                count++;
                break;
            }
        }
        return count;
    }

    private List<Player> playerThatCanNotChooseArchitect() {
        List<Player> playerThatDoNotChooseArchitect = new ArrayList<>();
        for (Player player : getListCopyPlayers()) {
            if ((player.getGolds() < 4 && (player.getBoard().size() < 6) && (player.getHands().size() < 2))) {
                playerThatDoNotChooseArchitect.add(player);
            }
        }
        return playerThatDoNotChooseArchitect;
    }

    @Override
    public int chooseCharacter(List<CharacterCard> cards) {
        discoverValidCard();

        //King
        if((cards.contains(CharacterCard.KING) && countNumberOfSpecifiedColorCard(Color.YELLOW) > 0) || (cards.contains(CharacterCard.KING) && this.isCrowned() && getListCopyPlayers().size() < 5)){
            return cards.indexOf(CharacterCard.KING);

        }
        else if (cards.contains(CharacterCard.WARLORD) && (countNumberOfSpecifiedColorCard(Color.RED)>0||(firstHas1GoldDistrict(getListCopyPlayers()) && getCurrentNbRound()>3))){
            return cards.indexOf(CharacterCard.WARLORD);
        }
        else if (!getDiscardedCardDuringTheRound().contains(CharacterCard.ARCHITECT)) {
            List<Player> playersThatCanNotChooseArchitect = playerThatCanNotChooseArchitect();
            List<Player> playersOrdered = getListCopyPlayers();
            for (Player player : playersThatCanNotChooseArchitect) {
                if (playersOrdered.indexOf(player) < playersOrdered.indexOf(this) && playersOrdered.get(playersOrdered.indexOf(player)).getPlayerRole() != CharacterCard.ARCHITECT) {
                    if (!getDiscardedCardDuringTheRound().contains(CharacterCard.ASSASSIN) && cards.contains(CharacterCard.ASSASSIN)) {
                        target = CharacterCard.ARCHITECT;
                        return cards.indexOf(CharacterCard.ASSASSIN);
                    }
                } else if (playersOrdered.indexOf(player) >= playersOrdered.indexOf(this) && getGolds() >= 4 && cards.contains(CharacterCard.ARCHITECT)) {
                    return cards.indexOf(CharacterCard.ARCHITECT);
                }
            }
        } else if (cards.contains(CharacterCard.BISHOP) && (countNumberOfSpecifiedColorCard(Color.BLUE) > 0 || (hasValidCard() && getCurrentNbRound() > 3))) {
            return cards.indexOf(CharacterCard.BISHOP);
        }
        else if((cards.contains(CharacterCard.MERCHANT) && countNumberOfSpecifiedColorCard(Color.GREEN) > 0) || (cards.contains(CharacterCard.MERCHANT) && getGolds() < 2)){
            return cards.indexOf(CharacterCard.MERCHANT);
        }
        else if(cards.contains(CharacterCard.MAGICIAN) && getHands().isEmpty() && thereIsSomeoneWithALotOfCards()){
            return cards.indexOf(CharacterCard.MAGICIAN);
        }
        //Thief is interesting at first but when the game progresses he is not interesting (according to tt-22a5e3f98e5243b9f1135d1caadc4cc7)
        else if (cards.contains(CharacterCard.THIEF) && getCurrentNbRound() <= 3 && getGolds() <= 2 && thereIsSomeoneWithALotOfGolds()) {
            return cards.indexOf(CharacterCard.THIEF);
        } else if (cards.contains(CharacterCard.ASSASSIN)) {
            if ((this.getHands().size() >= 5 && someoneHasNoCards(getListCopyPlayers()))) {
                target = CharacterCard.MAGICIAN;
                return cards.indexOf(CharacterCard.ASSASSIN);
            }
            //TODO
        }
        return random.nextInt(cards.size()); //return a random number between 0 and the size of the list
    }


    public boolean firstHas1GoldDistrict(List<Player> players) {

        for (Player player : players){
            if (player.equals(this))continue;
            if (numberOfDistrictOfFirstPlayer(players) == player.getBoard().size()){
                for (DistrictCard districtCard : player.getBoard()){
                    if (districtCard.getDistrictValue() == 1){
                        return true;
                    }
                }
            }
        }
        return false;
    }



    public boolean thereIsSomeoneWithALotOfGolds() {
        for (Player player : getListCopyPlayers()) {
            if (player.getGolds() >= 3 && player != this) {
                return true;
            }
        }
        return false;
    }

    public boolean thereIsSomeoneWithALotOfCards() {
        for (Player player : getListCopyPlayers()) {
            if (player.getHands().size() > this.getHands().size() && player != this) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Player choosePlayerToDestroy(List<Player> players) {
        List<Player> playersInOrder = numberOfDistrictInOrder(players);
        for (Player player : playersInOrder){
            if (player.equals(this)) continue;
            if (has1CardDistrictOnBoard(player)) return player;
        }
        return null;
    }

    public  boolean has1CardDistrictOnBoard(Player player){
        for (DistrictCard districtCard : player.getBoard()){
            if (districtCard.getDistrictValue() == 1){
                return true;
            }
        }
        return false;
    }

    @Override
    public DistrictCard chooseDistrictToDestroy(Player player, List<DistrictCard> districtCards) {
        for (DistrictCard districtCard : player.getBoard()) {
            if (districtCard.getDistrictValue() <= 1) return districtCard;
        }
        return null;
    }

    public void setRandom(Random random) {
        this.random = random;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public Color chooseColorForSchoolOfMagic() {
        if (getPlayerRole() == CharacterCard.KING || getPlayerRole() == CharacterCard.BISHOP || getPlayerRole() == CharacterCard.MERCHANT || getPlayerRole() == CharacterCard.WARLORD) {
            return getPlayerRole().getCharacterColor();
        }
        return Color.PURPLE;
    }
    @Override
    public Color chooseColorForHauntedCity() {
        Set<Color> colorsOnBoard = colorInList(getBoard());
        for (Color color : Color.values()) {
            if (!colorsOnBoard.contains(color)) {
                return color;
            }
        }
        return Color.PURPLE;
    }

    @Override
    public boolean wantToUseLaboratoryEffect() {
        discoverValidCard();
        for (DistrictCard card : this.getHands()) {
            if (!validCards.contains(card)) {
                return true;
            }
        }
        return false;
    }
    @Override
    public DistrictCard chooseHandCardToDiscard() {
        if(!getHands().isEmpty()) {
            ArrayList<DistrictCard> listOfCardsForSort = (ArrayList<DistrictCard>) getHands();
            DistrictCardComparator districtCardComparator = new DistrictCardComparator();
            listOfCardsForSort.sort(districtCardComparator);
            return listOfCardsForSort.get(0);
        }
        return null;
    }

    @Override
    public void drawCard(Map<DispatchState, ArrayList<DistrictCard>> cardsThatThePlayerDontWantAndThatThePlayerWant, DistrictCard... cards) {
        ArrayList<DistrictCard> listOfCardsForSort = new ArrayList<>(List.of(cards));
        LOGGER.info("Cartes piochées : " + Arrays.toString(cards));
        DistrictCardComparator districtCardComparator = new DistrictCardComparator();
        listOfCardsForSort.sort(districtCardComparator);
        for (int i = 0; i < listOfCardsForSort.size(); i++) {
            if (i == 0 || (this.getBoard().contains(DistrictCard.LIBRARY) && i == 1)) {
                cardsThatThePlayerDontWantAndThatThePlayerWant.get(DispatchState.CARDS_WANTED).add(listOfCardsForSort.get(listOfCardsForSort.size() - 1 - i));
            } else {
                cardsThatThePlayerDontWantAndThatThePlayerWant.get(DispatchState.CARDS_NOT_WANTED).add(listOfCardsForSort.get(listOfCardsForSort.size() - 1 - i));
            }
        }
        LOGGER.info("Cartes jetées : " + cardsThatThePlayerDontWantAndThatThePlayerWant.get(DispatchState.CARDS_NOT_WANTED));
    }



    @Override
    public DispatchState whichWarlordEffect(List<Player> players) {
        for (Player player : players) {
            for (DistrictCard districtCard : player.getBoard()) {
                if (districtCard.getDistrictValue() <= 1) return DispatchState.DESTROY;
            }
        }
        return DispatchState.EARNDISTRICT_WARLORD;
    }

    @Override
    public DispatchState whichMagicianEffect(List<Player> players) {
        int nbCardPlayer = this.getHands().size();
        for (Player p : players) {
            int nbCardOther = p.getHands().size();
            if (nbCardOther > nbCardPlayer) {
                return DispatchState.EXCHANGE_PLAYER;
            }
        }
        return DispatchState.EXCHANGE_DECK;
    }

    @Override
    public boolean wantToUseEffect(boolean beforePuttingADistrict) {
        discoverValidCard();
        for (DistrictCard districtCard : validCards) {
            if (districtCard.getDistrictColor() == this.getPlayerRole().getCharacterColor() && beforePuttingADistrict) {
                return getPlayerRole() == CharacterCard.WARLORD;
            }
        }
        return true;
    }

    @Override
    public boolean wantsToUseSmithyEffect() {
        return getGolds() >= 3 && validCards.isEmpty();
    }

    @Override
    public List<DistrictCard> chooseCardsToChange() {
        List<DistrictCard> districtCards = new ArrayList<>();
        for (DistrictCard districtCard : this.getHands()) {
            if (!validCards.contains(districtCard)) {
                districtCards.add(districtCard);
            }
        }
        return districtCards;
    }

    @Override
    public Player selectMagicianTarget(List<Player> players) {
        Player highNbCards = players.get(0);
        for (Player p : players) {
            //if equals we trade with someone who has the most district
            if ((p.getHands().size() == highNbCards.getHands().size() && p.getBoard().size() > highNbCards.getBoard().size()) || p.getHands().size() > highNbCards.getHands().size()) {
                highNbCards = p;
            }
        }
        return highNbCards;
    }

    @Override
    public boolean wantToUseGraveyardEffect() {
        return true;
    }

    public CharacterCard getTarget() {
        return target;
    }
}


