package fr.cotedazur.univ.polytech.controller;

import fr.cotedazur.univ.polytech.logger.LamaLogger;
import fr.cotedazur.univ.polytech.model.bot.DispatchState;
import fr.cotedazur.univ.polytech.model.bot.Player;
import fr.cotedazur.univ.polytech.model.card.CharacterCard;
import fr.cotedazur.univ.polytech.model.card.DistrictCard;
import fr.cotedazur.univ.polytech.model.card.PurpleEffectState;
import fr.cotedazur.univ.polytech.model.deck.Deck;
import fr.cotedazur.univ.polytech.model.deck.DeckFactory;
import fr.cotedazur.univ.polytech.model.golds.StackOfGolds;
import fr.cotedazur.univ.polytech.view.GameView;

import java.util.*;

public class Round {
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(LamaLogger.class.getName());
    private final EffectController effectController;
    private List<Player> players;
    private List<Player> playersSortedByCharacterNumber;
    private GameView view;
    //Decks
    private Deck<DistrictCard> districtDeck;
    private Deck<DistrictCard> districtDiscardDeck; // This deck will be used when the warlord destroy a district or when the magician swap his hand with the deck
    private Deck<CharacterCard> characterDeck;
    private Deck<CharacterCard> faceUpCharactersDiscarded;
    private CharacterCard faceDownCharacterDiscarded;
    private int nbRound;
    private StackOfGolds stackOfGolds;

    public Round(List<Player> players, GameView view, Deck<DistrictCard> districtDeck, Deck<DistrictCard> districtDiscardDeck, int nbRound, StackOfGolds stackOfGolds) {
        this.players = players;
        this.playersSortedByCharacterNumber = new ArrayList<>(players);
        this.view = view;

        // Keep the decks for the districts
        this.districtDeck = districtDeck;
        this.districtDiscardDeck = districtDiscardDeck;

        // New decks for the characters
        this.characterDeck = DeckFactory.createCharacterDeck();
        characterDeck.shuffle();
        this.faceUpCharactersDiscarded = DeckFactory.createEmptyCharacterDeck();

        this.nbRound = nbRound;

        this.stackOfGolds = stackOfGolds;

        //reset the players effect boolean
        for (Player player : players) {
            player.setUsedEffect("");
            player.setDead(false);
            player.setHasBeenStolen(false);
        }

        effectController = new EffectController(view, stackOfGolds);
    }

    public Round(GameView view, StackOfGolds stackOfGolds) {
        effectController = new EffectController(view, stackOfGolds);
    }

    /**
     * Play the round
     */
    public void startRound() {
        //Announce the start of the round
        LOGGER.info("Début du round " + nbRound);
        view.printStartRound(nbRound);

        //Discard cards
        discardCards();

        for (Player player : players) {
            player.setListCopyPlayers(effectController.playerNeededWithoutSensibleInformation(players, player));
        }

        //Each player choose a character
        choiceOfCharactersForEachPlayer();

        //Set the new crowned player if there is one
        setNewCrownedPlayer();

        //Print the recap of all players
        view.printRecapOfAllPlayers(players);

        //Print the board of all players
        view.printBoardOfAllPlayers(players);

        //Sort the players by the number of the character card
        sortPlayersByNumbersOfCharacterCard();

        //Each player make a choice (draw a card or take 2 golds) and put a district
        choiceActionsForTheRound();

        //Reset the RoleKilled for all the players
        for (Player player : players) {
            player.setRoleKilledByAssassin(null);
        }

        //Announce the end of the round
        view.printEndRound(nbRound);
    }

    /**
     * Discard cards at the start of the round
     * 4 players : 2 cards face-up and 1 face-down
     * 5 players : 1 cards face-up and 1 face-down
     * 6 players : 0 card face-up and 1 face-down
     * 7 players : 0 card face-up and 1 face-down
     */
    public void discardCards() {
        int numberOfPlayers = players.size();

        if (numberOfPlayers < 6) {
            for (int i = numberOfPlayers - 4; i < 2; i++) {
                CharacterCard drawnCard = characterDeck.draw();

                //King can't be discarded face-up
                if (drawnCard == CharacterCard.KING) {
                    drawnCard = characterDeck.draw();
                    characterDeck.add(CharacterCard.KING);
                }

                faceUpCharactersDiscarded.add(drawnCard);
            }

            view.printDiscardedCardFaceUp(faceUpCharactersDiscarded);
        }

        //1 card has to be discarded face-down
        faceDownCharacterDiscarded = characterDeck.draw();
        LOGGER.info("Carte defaussée face cachée : " + faceDownCharacterDiscarded.getCharacterName());
    }

    /**
     * Sort the players by the number of the character card
     */
    public void sortPlayersByNumbersOfCharacterCard() {
        playersSortedByCharacterNumber.sort(Comparator.comparingInt(player -> player.getPlayerRole().getCharacterNumber()));
    }

    /**
     * Function that allows each player to choose a character in the list of character available
     */
    public void choiceOfCharactersForEachPlayer() {
        int i = 0;
        for (Player player : players) {
            //while the player has not chosen a character (or the character is not available)
            boolean again = true;
            while (again) {
                //Print the all character cards in the deck
                view.printPlayerPickACard(player.getName(), characterDeck.getCards());

                // Case where there is 7 players, the last player recover the face-down card to choose his character
                if (i == 6) {
                    view.printCharacterCard(faceDownCharacterDiscarded.getCharacterNumber(), faceDownCharacterDiscarded.getCharacterName(), faceDownCharacterDiscarded.getCharacterEffect());
                    characterDeck.add(faceDownCharacterDiscarded);
                }

                //Store the information relative to the choice of the characters for the bot
                player.setCurrentChoiceOfCharactersCardsDuringTheRound(characterDeck.getCards());
                player.setDiscardedCardDuringTheRound(faceUpCharactersDiscarded.getCards());
                player.setCurrentNbRound(nbRound);

                int characterNumber = player.chooseCharacter(characterDeck.getCards());
                CharacterCard drawn = characterDeck.draw(characterNumber);
                //If the card is not available, the player choose again, after an error message
                if (drawn == null) {
                    view.pickARoleCardError();
                } else {
                    //Else, we set the role of the player and print the character card chosen
                    again = false;
                    player.setPlayerRole(drawn);
                    view.printEndOfPicking(player.getName());
                }
            }
            i++;
        }
    }

    /**
     * Function that allows each player to choose their actions for the current round (choose 2golds or draw a card and choose to put a district or not)
     */
    public void choiceActionsForTheRound() {
        for (Player player : playersSortedByCharacterNumber) {
            if (player.isDead()) {
                LOGGER.info("Le joueur " + player.getName() + " est mort, il ne peut pas jouer");
                continue;
            }

            //Reveal the role for all the players
            for(Player player1 : players){
                for(Player player2 : player1.getListCopyPlayers()){
                    if(player2.getName().equals(player.getName())){
                        player2.setPlayerRole(player.getPlayerRole());
                    }
                }
            }

            //If player is dead he will not be stolen
            if (player.isStolen()) {
                effectController.getPlayerWhoStole().getPlayerRole().useEffectThief(effectController.getPlayerWhoStole(), player, true);
            }


            this.drawOr2golds(player);

            // We play the district cards effects
            playDistrictCards(player);

            // We play the character cards effects before the player put a district
            playerWillingToUseEffect(player, true);

            // Draw and place a district
            int i = 0;
            int maxDistrictThatCanBePut = 1;
            if (player.getPlayerRole() == CharacterCard.ARCHITECT) maxDistrictThatCanBePut = 3;
            while (i++ < maxDistrictThatCanBePut) {
                this.putDistrictForPlayer(player);
            }

            // We play the character cards effects after the player put a district
            playerWillingToUseEffect(player, false);

            // Display the effect of the character card
            view.printCharacterUsedEffect(player);

            determineFirstPlayerTo8Districts(player);
            view.printEndTurnOfPlayer(player);
        }
    }

    /**
     * Function that allows each player to play his district cards
     *
     * @param player the player who will play his district cards
     */
    private void playDistrictCards(Player player) {
        if (player.hasCardOnTheBoard(DistrictCard.SMITHY) && player.getGolds() >= 3 && !districtDeck.isEmpty() && (player.wantsToUseSmithyEffect())) {
            view.printPurpleEffect(player, PurpleEffectState.SMITHY_EFFECT);
            player.setGolds(player.getGolds() - 3);
            stackOfGolds.addGoldsToStack(3);
            for (int i = 0; i < 3; i++) {
                if (i < districtDeck.size()) player.addCardToHand(districtDeck.draw());
            }
        }

        //Because architect automatically take +2 cards
        if (player.getPlayerRole() == CharacterCard.ARCHITECT)
            player.getPlayerRole().useEffectArchitect(player, districtDeck);

        //Because Merchant automatically take +1 gold
        if (player.getPlayerRole() == CharacterCard.MERCHANT)
            player.setGolds(player.getGolds() + stackOfGolds.takeAGold());

        // If the player has a laboratory, he can discard a card to earn 1 gold
        if (player.hasCardOnTheBoard(DistrictCard.LABORATORY) && !player.getHands().isEmpty() && player.wantToUseLaboratoryEffect()) {
            DistrictCard cardToRemove = player.chooseHandCardToDiscard();
            if (cardToRemove != null) {
                player.getHands().remove(cardToRemove);
                player.setGolds(player.getGolds() + stackOfGolds.takeAGold());
                view.printPurpleEffect(player, PurpleEffectState.LABORATORY_EFFECT);
            }
        }

        // If the player has the haunted city, we set the round where he put the haunted city
        if (player.hasCardOnTheBoard(DistrictCard.HAUNTED_CITY) && player.getWhatIsTheRoundWhereThePlayerPutHisHauntedCity() == 0)
            player.setWhatIsTheRoundWhereThePlayerPutHisHauntedCity(nbRound);
    }

    /**
     * Set the first player to 8 districts by checking if the player has 8 districts and if no player has already been set as first to 8 districts
     *
     * @param player the player to check
     */
    private void determineFirstPlayerTo8Districts(Player player) {
        if (player.getBoard().size() >= 8 && noPlayerAddCompleteFirst()) player.setFirstToAdd8district(true);
    }

    /**
     * Function that allows each player to choose if he wants to use his effect
     *
     * @param player                 the player who will use his effect
     * @param beforePuttingADistrict true if the player want to use his effect before putting a district, false otherwise
     */
    private void playerWillingToUseEffect(Player player, boolean beforePuttingADistrict) {
        if (player.wantToUseEffect(beforePuttingADistrict) && player.getPlayerRole() != CharacterCard.ARCHITECT) {
            effectController.playerWantToUseEffect(player, playersSortedByCharacterNumber, districtDiscardDeck, districtDeck);
            if (player.getPlayerRole() == CharacterCard.WARLORD)
                effectController.playerWantToUseEffect(player, playersSortedByCharacterNumber, districtDiscardDeck, districtDeck);
        }
    }

    /**
     * Function that allows each player to choose if he wants to draw a card or take 2 golds
     *
     * @param player the player who will choose to draw a card or take 2 golds
     */
    public void drawOr2golds(Player player) {
        DispatchState choice = null;

        //Take the choice
        while (choice == null) {
            choice = player.startChoice();
            if (choice.equals(DispatchState.DRAW_CARD) && districtDeck.isEmpty()) choice = DispatchState.TWO_GOLDS;
            if (choice.equals(DispatchState.TWO_GOLDS) && stackOfGolds.getNbGolds() == 0)
                choice = DispatchState.CANT_PLAY;
        }

        //Process the choice
        if (choice.equals(DispatchState.TWO_GOLDS)) {
            this.collectTwoGoldsForPlayer(player);
        } else if (choice.equals(DispatchState.DRAW_CARD)) {
            playerWantToDrawCard(player);
        }
        view.printPlayerAction(choice, player);
    }

    /**
     * Function that allows each player to draw a card
     *
     * @param player the player who will draw a card
     */
    public void playerWantToDrawCard(Player player) {
        ArrayList<DistrictCard> cardsThatPlayerDraw = new ArrayList<>();
        int nbCardToDraw = player.getBoard().contains(DistrictCard.OBSERVATORY) ? 3 : 2;
      
        if (nbCardToDraw == 3)
            view.printPurpleEffect(player, PurpleEffectState.OBSERVATORY_EFFECT);

        drawCards(cardsThatPlayerDraw, nbCardToDraw);

        Map<DispatchState, ArrayList<DistrictCard>> cardsThatThePlayerDontWantAndThatThePlayerWant = new EnumMap<>(DispatchState.class);
        cardsThatThePlayerDontWantAndThatThePlayerWant.put(DispatchState.CARDS_WANTED, new ArrayList<>());
        cardsThatThePlayerDontWantAndThatThePlayerWant.put(DispatchState.CARDS_NOT_WANTED, new ArrayList<>());

        dispatchCards(player, cardsThatPlayerDraw, cardsThatThePlayerDontWantAndThatThePlayerWant);

        handleRemainingCards(cardsThatThePlayerDontWantAndThatThePlayerWant);
    }

    private void drawCards(ArrayList<DistrictCard> cardsThatPlayerDraw, int nbCardToDraw) {
        for (int i = 0; i < nbCardToDraw; i++) {
            if (!districtDeck.isEmpty())
                cardsThatPlayerDraw.add(districtDeck.draw());
        }
    }

    private void dispatchCards(Player player, ArrayList<DistrictCard> cardsThatPlayerDraw, Map<DispatchState, ArrayList<DistrictCard>> cardsThatThePlayerDontWantAndThatThePlayerWant) {
        int numberOfCards = cardsThatPlayerDraw.size();
        if (numberOfCards > 0) {
            DistrictCard[] cardsArray = cardsThatPlayerDraw.toArray(new DistrictCard[0]);
            player.drawCard(cardsThatThePlayerDontWantAndThatThePlayerWant, cardsArray);
        }
    }

    private void handleRemainingCards(Map<DispatchState, ArrayList<DistrictCard>> cardsThatThePlayerDontWantAndThatThePlayerWant) {
        ArrayList<DistrictCard> cardsNotWanted = cardsThatThePlayerDontWantAndThatThePlayerWant.get(DispatchState.CARDS_NOT_WANTED);
        if (!cardsNotWanted.isEmpty()) {
            for (DistrictCard card : cardsNotWanted) {
                districtDeck.add(card);
            }
        }
    }

    /**
     * Check if no player has already been set as first to 8 districts
     *
     * @return true if no player has already been set as first to 8 districts, false otherwise
     */
    public boolean noPlayerAddCompleteFirst() {
        for (Player player : players) {
            if (player.isFirstToAdd8district()) return false;
        }
        return true;
    }

    /**
     * Set the crown to the new king
     */
    public void setNewCrownedPlayer() {
        boolean kingFound = false;
        for (Player player : players) {
            if (player.getPlayerRole() == CharacterCard.KING) {
                kingFound = true;
                break;
            }
        }
        if (kingFound) {
            for (Player player : players) {
                player.setCrowned(player.getPlayerRole() == CharacterCard.KING);
            }
        }
    }

    public Deck<CharacterCard> getCharacterDiscardDeck() {
        return faceUpCharactersDiscarded;
    }

    /**
     * Collect two golds or less for the player
     *
     * @param player the player who will collect the golds
     */
    public void collectTwoGoldsForPlayer(Player player) {
        int nbMaxCoins = 0;
        for (int i = 0; i < 2; i++) {
            nbMaxCoins += stackOfGolds.takeAGold();
        }
        player.setGolds(player.getGolds() + nbMaxCoins);
    }

    /**
     * Put a district for the player
     *
     * @param player the player who will put the district
     */
    public void putDistrictForPlayer(Player player) {
        DistrictCard districtToPut;
        do {
            districtToPut = player.choiceHowToPlayDuringTheRound();
        } while (player.hasCardOnTheBoard(districtToPut) && player.hasPlayableCard());
        if (districtToPut != null && !player.hasCardOnTheBoard(districtToPut)) {
            player.addCardToBoard(districtToPut);
            player.removeGold(districtToPut.getDistrictValue());
            this.stackOfGolds.addGoldsToStack(districtToPut.getDistrictValue());
            if (view != null) view.printPlayerAction(DispatchState.PLACE_DISTRICT, player);
        }
    }

    public StackOfGolds getStackOfGolds() {
        return stackOfGolds;
    }
}
