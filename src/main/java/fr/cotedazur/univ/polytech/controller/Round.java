package fr.cotedazur.univ.polytech.controller;

import fr.cotedazur.univ.polytech.model.bot.Player;
import fr.cotedazur.univ.polytech.model.card.DistrictCard;
import fr.cotedazur.univ.polytech.model.card.CharacterCard;
import fr.cotedazur.univ.polytech.model.deck.Deck;
import fr.cotedazur.univ.polytech.model.deck.DistrictDeck;
import fr.cotedazur.univ.polytech.view.GameView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class Round {
    private final List<Player> players;
    private final List<Player> playersSortedByCharacterNumber;
    private final GameView view;

    //Decks
    private final Deck<DistrictCard> districtDeck;
    private final Deck<DistrictCard> districtDiscardDeck;
    private final Deck<CharacterCard> characterDeck;
    private final Deck<CharacterCard> characterDiscardDeck;

    private CharacterCard faceDownCharacter;
    private final int nbRound;

    public Round(List<Player> players, GameView view, Deck<DistrictCard> districtDeck, Deck<DistrictCard> districtDiscardDeck, Deck<CharacterCard> characterDeck, Deck<CharacterCard> characterDiscardDeck, int nbRound) {
        this.players = players;
        this.playersSortedByCharacterNumber = new ArrayList<>(players);
        this.view = view;
        this.districtDeck = districtDeck;
        this.districtDiscardDeck = districtDiscardDeck;
        this.characterDeck = characterDeck;
        this.characterDiscardDeck = characterDiscardDeck;
        this.nbRound = nbRound;
    }

    /**
     * Play the round
     */
    public void startRound() {
        //Announce the start of the round
        view.printStartRound(nbRound);

        int numberOfPlayers = players.size();

        characterDeck.shuffle();


        //2 card has to be discarded face-up if there is 4 players and 1 if they are 5
        if (numberOfPlayers < 6) {
            for (int i = numberOfPlayers-4; i < 2;i++){
                CharacterCard drawnCard = characterDeck.draw();
                //King can't be discarded face-up
                if (drawnCard == CharacterCard.KING) {
                    drawnCard = characterDeck.draw();
                    characterDeck.add(CharacterCard.KING);

                }
                characterDiscardDeck.add(drawnCard);

            }
            view.printDiscardedCard(characterDiscardDeck);
        }

        //1 card has to be discarded face-down
        faceDownCharacter = characterDeck.draw();

        //Each player choose a character
        choiceOfCharactersForEachPlayer();

        // Set the new crowned player if there is one
        setNewCrownedPlayer();

        //Sort the players by the number of the character card
        sortPlayersByNumbersOfCharacterCard();

        //Each player make a choice (draw a card or take 2 golds) and put a district
        choiceActionsForTheRound();

        //Announce the end of the round
        view.printEndRound(nbRound);
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
        for (Player player: players){
            //while the player has not chosen a character (or the character is not available)
            boolean again = true;
            while (again) {
                //Print the all character cards in the deck
                view.printPlayerPickACard(player.getName());
                for (CharacterCard character : characterDeck.getCards()) {
                    view.printCharacterCard(character.getCharacterNumber(), character.getCharacterName(), character.getCharacterEffect());
                }
                if (i == 6){
                    view.printCharacterCard(faceDownCharacter.getCharacterNumber(), faceDownCharacter.getCharacterName(), faceDownCharacter.getCharacterEffect());
                    characterDeck.add(faceDownCharacter);
                }
                int characterNumber = player.chooseCharacter(characterDeck.getCards());
                CharacterCard drawn = characterDeck.draw(characterNumber);
                //If the card is not available, the player choose again, after an error message
                if (drawn == null) {
                    view.pickARoleCardError();
                } else {
                    //Else, we set the role of the player and print the character card chosen
                    again = false;
                    player.setPlayerRole(drawn);
                    view.printCharacterCard(drawn.getCharacterName());
                }

            }
            i++;
        }
    }

    /**
     * Function that allows each player to choose their actions for the current round (choose 2golds or draw a card and choose to put a district or not)
     */
    public void choiceActionsForTheRound() {
        String choice;
        for (Player player : playersSortedByCharacterNumber) {
            //Take the choice
            choice = player.startChoice((DistrictDeck) districtDeck);
            if (choice != null) view.printPlayerAction(choice , player);

            // Draw and place a district
            player.drawAndPlaceADistrict(view);

            //Special case of the architect because he can put 2 more districts
            if(player.getPlayerRole() == CharacterCard.ARCHITECT)  player.useRoleEffect(Optional.empty(), Optional.of(view));


            view.printEndTurnOfPlayer(player);
        }
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
                if (player.getPlayerRole() == CharacterCard.KING) {
                    player.setCrowned(true);
                }
                else player.setCrowned(false);
            }
        }
    }
}
