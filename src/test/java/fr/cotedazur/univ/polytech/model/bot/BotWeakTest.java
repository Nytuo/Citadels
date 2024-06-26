package fr.cotedazur.univ.polytech.model.bot;

import fr.cotedazur.univ.polytech.controller.EffectController;
import fr.cotedazur.univ.polytech.logger.LamaLogger;
import fr.cotedazur.univ.polytech.model.card.CharacterCard;
import fr.cotedazur.univ.polytech.model.card.Color;
import fr.cotedazur.univ.polytech.model.card.DistrictCard;
import fr.cotedazur.univ.polytech.model.deck.Deck;
import fr.cotedazur.univ.polytech.model.deck.DeckFactory;
import fr.cotedazur.univ.polytech.model.golds.StackOfGolds;
import fr.cotedazur.univ.polytech.view.GameView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class BotWeakTest {

    Player botWeak;
    Deck<DistrictCard> districtDeck;

    Map<DispatchState, ArrayList<DistrictCard>> cardsThatThePlayerDontWantAndThatThePlayerWant = new EnumMap<>(DispatchState.class);


    @BeforeEach
    void setUp() {
        LamaLogger.mute();
        botWeak = new BotWeak();
        this.districtDeck = DeckFactory.createDistrictDeck();
        this.districtDeck.shuffle();
        cardsThatThePlayerDontWantAndThatThePlayerWant.put(DispatchState.CARDS_WANTED, new ArrayList<>());
        cardsThatThePlayerDontWantAndThatThePlayerWant.put(DispatchState.CARDS_NOT_WANTED, new ArrayList<>());
    }

    @Test
    void putADistrict() {
        assertNull(botWeak.putADistrict());
        botWeak.getHands().add(DistrictCard.PALACE);
        botWeak.getHands().add(DistrictCard.TEMPLE);
        botWeak.getHands().add(DistrictCard.GRAVEYARD);
        botWeak.getHands().add(DistrictCard.MARKET);
        botWeak.setGolds(20);
        botWeak.setPlayerRole(CharacterCard.ASSASSIN);

        //Should be Temple because its value are the smallest of the botWeak hand
        botWeak.addCardToBoard(botWeak.choiceHowToPlayDuringTheRound());
        assertEquals(DistrictCard.TEMPLE, botWeak.getBoard().get(0));

        //Should be Market because its value are now the smallest of the botWeak hand
        botWeak.addCardToBoard(botWeak.choiceHowToPlayDuringTheRound());
        assertEquals(DistrictCard.MARKET, botWeak.getBoard().get(1));
        botWeak.getHands().clear();

        //When District are equals
        botWeak.getHands().add(DistrictCard.MONASTERY);
        botWeak.getHands().add(DistrictCard.MANOR);
        botWeak.getHands().add(DistrictCard.KEEP);
        botWeak.getHands().add(DistrictCard.DOCKS);

        botWeak.addCardToBoard(botWeak.choiceHowToPlayDuringTheRound());

        //Should be Monastery because there are all equals and the order doesn't change
        assertEquals(DistrictCard.MONASTERY, botWeak.getBoard().get(2));
    }

    @Test
    void testUseEffectForBotWeak() {
        //Draw with architect
        botWeak.setPlayerRole(CharacterCard.ARCHITECT);
        botWeak.getPlayerRole().useEffectArchitect(botWeak, districtDeck);
        assertEquals(2, botWeak.getHands().size());
        botWeak.getHands().clear();

        /*//Put 3 district with architect
        botWeak.getHands().add(DistrictCard.PALACE);
        botWeak.getHands().add(DistrictCard.PRISON);
        botWeak.getHands().add(DistrictCard.MANOR);
        botWeak.setGolds(24);
        botWeak.addCardToBoard(botWeak.choiceHowToPlayDuringTheRound());
        botWeak.getPlayerRole().useEffect(botWeak,(Player) null);
        assertEquals(3, botWeak.getBoard().size());
        botWeak.getHands().clear();
        botWeak.getBoard().clear();*/

        /*//Trying to put 3 district with architect but gold are reduced
        botWeak.getHands().add(DistrictCard.PALACE);
        botWeak.getHands().add(DistrictCard.PRISON);
        botWeak.getHands().add(DistrictCard.MANOR);
        botWeak.setGolds(9);
        botWeak.addCardToBoard(botWeak.choiceHowToPlayDuringTheRound());
        botWeak.getPlayerRole().useEffect(botWeak,(Player) null);
        assertEquals(2, botWeak.getBoard().size());
        botWeak.getHands().clear();
        botWeak.getBoard().clear();*/

        //Use merchant effect
        botWeak.setPlayerRole(CharacterCard.MERCHANT);
        botWeak.getHands().add(DistrictCard.PALACE);
        botWeak.getHands().add(DistrictCard.TOWN_HALL);
        botWeak.getHands().add(DistrictCard.MARKET);
        botWeak.setGolds(5);
        botWeak.getPlayerRole().useEffect(botWeak, new StackOfGolds(), null);
        assertEquals(5, botWeak.getGolds());
        botWeak.addCardToBoard(DistrictCard.TOWN_HALL);
        botWeak.addCardToBoard(DistrictCard.MARKET);
        botWeak.setGolds(5);
        botWeak.getPlayerRole().useEffect(botWeak, new StackOfGolds(), null);
        assertEquals(7, botWeak.getGolds());
        botWeak.getHands().clear();
        botWeak.getBoard().clear();

        //Use Thief effect
        botWeak.setGolds(5);
        botWeak.setPlayerRole(CharacterCard.THIEF);
        ArrayList<Player> players = new ArrayList<>();
        Player player = new BotWeak();
        player.setGolds(31);
        player.setPlayerRole(CharacterCard.BISHOP);
        players.add(player);
        Player player2 = new BotWeak();
        player2.setGolds(32);
        player2.setPlayerRole(CharacterCard.ASSASSIN);
        players.add(player2);
        Player player3 = new BotWeak();
        player3.setGolds(33);
        player3.setPlayerRole(CharacterCard.MERCHANT);
        players.add(player3);

        EffectController effectController = new EffectController(new GameView(), new StackOfGolds());
        effectController.playerWantToUseEffect(botWeak, players, new Deck<>(), districtDeck);

        assertTrue(player3.isStolen());

        players.clear();

        for (CharacterCard characterCard : CharacterCard.values()) {
            if (characterCard != CharacterCard.ASSASSIN) {
                Player currentPlayer = new BotWeak();
                currentPlayer.setGolds(10);
                currentPlayer.setPlayerRole(characterCard);
                players.add(currentPlayer);
            }
        }

        botWeak.setPlayerRole(CharacterCard.ASSASSIN);
        EffectController effectController2 = new EffectController();
        effectController2.playerWantToUseEffect(botWeak, players, new Deck<>(), districtDeck);
        assertTrue(players.get(6).isDead());
        for (Player player1 : players) {
            if (player1.getPlayerRole() != CharacterCard.ASSASSIN && player1.getPlayerRole() != players.get(6).getPlayerRole()) {
                assertFalse(player1.isDead());
            }
        }
    }

    @Test
    void testUseEffectForSchoolOfMagic() {
        EffectController effectController = new EffectController(new GameView(), new StackOfGolds());
        botWeak.setPlayerRole(CharacterCard.MERCHANT);
        botWeak.getHands().add(DistrictCard.PALACE);
        botWeak.getHands().add(DistrictCard.TOWN_HALL);
        botWeak.getHands().add(DistrictCard.SCHOOL_OF_MAGIC);
        botWeak.setGolds(5);
        botWeak.getPlayerRole().useEffect(botWeak, new StackOfGolds(), null);
        assertEquals(5, botWeak.getGolds());
        botWeak.addCardToBoard(DistrictCard.TOWN_HALL);
        botWeak.addCardToBoard(DistrictCard.SCHOOL_OF_MAGIC);
        botWeak.setGolds(5);
        botWeak.getPlayerRole().useEffect(botWeak, new StackOfGolds(), effectController.verifyPresenceOfSchoolOfMagicCard(botWeak));
        assertEquals(7, botWeak.getGolds());
        botWeak.getHands().clear();
        botWeak.getBoard().clear();

        botWeak.setPlayerRole(CharacterCard.WARLORD);
        botWeak.getHands().add(DistrictCard.PALACE);
        botWeak.getHands().add(DistrictCard.TOWN_HALL);
        botWeak.getHands().add(DistrictCard.SCHOOL_OF_MAGIC);
        botWeak.setGolds(5);
        botWeak.getPlayerRole().useEffect(botWeak, new StackOfGolds(), effectController.verifyPresenceOfSchoolOfMagicCard(botWeak));
        assertEquals(5, botWeak.getGolds());
        botWeak.addCardToBoard(DistrictCard.TOWN_HALL);
        botWeak.addCardToBoard(DistrictCard.SCHOOL_OF_MAGIC);
        botWeak.setGolds(5);
        botWeak.getPlayerRole().useEffect(botWeak, new StackOfGolds(), effectController.verifyPresenceOfSchoolOfMagicCard(botWeak));
        assertEquals(6, botWeak.getGolds());
        botWeak.getHands().clear();
        botWeak.getBoard().clear();

        botWeak.setPlayerRole(CharacterCard.BISHOP);
        botWeak.getHands().add(DistrictCard.PALACE);
        botWeak.getHands().add(DistrictCard.TOWN_HALL);
        botWeak.getHands().add(DistrictCard.SCHOOL_OF_MAGIC);
        botWeak.setGolds(5);
        botWeak.getPlayerRole().useEffect(botWeak, new StackOfGolds(), effectController.verifyPresenceOfSchoolOfMagicCard(botWeak));
        assertEquals(5, botWeak.getGolds());
        botWeak.addCardToBoard(DistrictCard.TOWN_HALL);
        botWeak.addCardToBoard(DistrictCard.SCHOOL_OF_MAGIC);
        botWeak.setGolds(5);
        botWeak.getPlayerRole().useEffect(botWeak, new StackOfGolds(), effectController.verifyPresenceOfSchoolOfMagicCard(botWeak));
        assertEquals(6, botWeak.getGolds());
        botWeak.getHands().clear();
        botWeak.getBoard().clear();

        botWeak.setPlayerRole(CharacterCard.KING);
        botWeak.getHands().add(DistrictCard.PALACE);
        botWeak.getHands().add(DistrictCard.TOWN_HALL);
        botWeak.getHands().add(DistrictCard.SCHOOL_OF_MAGIC);
        botWeak.setGolds(5);
        botWeak.getPlayerRole().useEffect(botWeak, new StackOfGolds(), effectController.verifyPresenceOfSchoolOfMagicCard(botWeak));
        assertEquals(5, botWeak.getGolds());
        botWeak.addCardToBoard(DistrictCard.TOWN_HALL);
        botWeak.addCardToBoard(DistrictCard.SCHOOL_OF_MAGIC);
        botWeak.setGolds(5);
        botWeak.getPlayerRole().useEffect(botWeak, new StackOfGolds(), effectController.verifyPresenceOfSchoolOfMagicCard(botWeak));
        assertEquals(6, botWeak.getGolds());
        botWeak.getHands().clear();
        botWeak.getBoard().clear();


        botWeak.setPlayerRole(CharacterCard.ASSASSIN);
        botWeak.getHands().add(DistrictCard.PALACE);
        botWeak.getHands().add(DistrictCard.TOWN_HALL);
        botWeak.getHands().add(DistrictCard.SCHOOL_OF_MAGIC);
        botWeak.setGolds(5);
        botWeak.getPlayerRole().useEffect(botWeak, new StackOfGolds(), effectController.verifyPresenceOfSchoolOfMagicCard(botWeak));
        assertEquals(5, botWeak.getGolds());
        botWeak.addCardToBoard(DistrictCard.TOWN_HALL);
        botWeak.addCardToBoard(DistrictCard.SCHOOL_OF_MAGIC);
        botWeak.setGolds(5);
        botWeak.getPlayerRole().useEffect(botWeak, new StackOfGolds(), effectController.verifyPresenceOfSchoolOfMagicCard(botWeak));
        assertEquals(5, botWeak.getGolds());
        botWeak.getHands().clear();
        botWeak.getBoard().clear();

        botWeak.setPlayerRole(CharacterCard.THIEF);
        botWeak.getHands().add(DistrictCard.PALACE);
        botWeak.getHands().add(DistrictCard.TOWN_HALL);
        botWeak.getHands().add(DistrictCard.SCHOOL_OF_MAGIC);
        botWeak.setGolds(5);
        botWeak.getPlayerRole().useEffect(botWeak, new StackOfGolds(), effectController.verifyPresenceOfSchoolOfMagicCard(botWeak));
        assertEquals(5, botWeak.getGolds());
        botWeak.addCardToBoard(DistrictCard.TOWN_HALL);
        botWeak.addCardToBoard(DistrictCard.SCHOOL_OF_MAGIC);
        botWeak.setGolds(5);
        botWeak.getPlayerRole().useEffect(botWeak, new StackOfGolds(), effectController.verifyPresenceOfSchoolOfMagicCard(botWeak));
        assertEquals(5, botWeak.getGolds());
        botWeak.getHands().clear();
        botWeak.getBoard().clear();
    }

    @Test
    void testChooseCharacter() {
        Deck<CharacterCard> characterDeck = DeckFactory.createCharacterDeck();
        botWeak.getHands().add(DistrictCard.SMITHY);
        botWeak.getHands().add(DistrictCard.PALACE);
        botWeak.getHands().add(DistrictCard.TOWN_HALL);
        botWeak.getHands().add(DistrictCard.MARKET);

        botWeak.addCardToBoard(DistrictCard.MARKET);
        botWeak.addCardToBoard(DistrictCard.TOWN_HALL);
        botWeak.addCardToBoard(DistrictCard.PALACE);
        assertEquals(CharacterCard.MERCHANT, characterDeck.getCards().get(botWeak.chooseCharacter(characterDeck.getCards())));

        //Test when merchant is not in list of characters
        characterDeck.getCards().remove(CharacterCard.MERCHANT);
        assertEquals(CharacterCard.KING, characterDeck.getCards().get(botWeak.chooseCharacter(characterDeck.getCards())));

        //Test with architect
        botWeak.getHands().add(DistrictCard.PALACE);
        botWeak.getHands().add(DistrictCard.TOWN_HALL);
        botWeak.getHands().add(DistrictCard.MARKET);
        //Can be architect because there are duplicates cards
        assertNotEquals(CharacterCard.ARCHITECT, characterDeck.getCards().get(botWeak.chooseCharacter(characterDeck.getCards())));
        botWeak.getHands().clear();
        botWeak.getBoard().clear();
        //Now we test if he chose the architect
        botWeak.getHands().add(DistrictCard.SMITHY);
        botWeak.getHands().add(DistrictCard.PALACE);
        botWeak.getHands().add(DistrictCard.TOWN_HALL);
        botWeak.getHands().add(DistrictCard.MARKET);
        botWeak.setGolds(80);
        assertEquals(CharacterCard.ARCHITECT, characterDeck.getCards().get(botWeak.chooseCharacter(characterDeck.getCards())));
    }

    @Test
    void testStartChoice() {
        //Test when we can put a district
        botWeak.getHands().add(DistrictCard.SMITHY);
        botWeak.getHands().add(DistrictCard.PALACE);
        botWeak.getHands().add(DistrictCard.TOWN_HALL);
        botWeak.getHands().add(DistrictCard.MARKET);
        botWeak.setGolds(80);
        botWeak.setPlayerRole(CharacterCard.ASSASSIN);
        assertEquals(DispatchState.DRAW_CARD, botWeak.startChoice());

        //when there is not enough golds
        botWeak.setGolds(0);
        assertEquals(DispatchState.TWO_GOLDS, botWeak.startChoice());

        //when hand is empty
        botWeak.setGolds(80);
        botWeak.getHands().clear();
        assertEquals(DispatchState.DRAW_CARD, botWeak.startChoice());

        botWeak.setGolds(4);
        botWeak.getHands().add(DistrictCard.MARKET);
        assertEquals(DispatchState.TWO_GOLDS, botWeak.startChoice());
    }

    @Test
    void testDrawCard() {
        botWeak.drawCard(cardsThatThePlayerDontWantAndThatThePlayerWant, DistrictCard.CASTLE, DistrictCard.PALACE, DistrictCard.TAVERN, DistrictCard.MARKET);//Just for the test we add 4 cards
        ArrayList<DistrictCard> cardTakenByTheBotWeak = cardsThatThePlayerDontWantAndThatThePlayerWant.get(DispatchState.CARDS_WANTED);
        ArrayList<DistrictCard> cardsDontTakenByTheBotWeak = cardsThatThePlayerDontWantAndThatThePlayerWant.get(DispatchState.CARDS_NOT_WANTED);

        assertTrue(cardsDontTakenByTheBotWeak.contains(DistrictCard.CASTLE));
        assertTrue(cardsDontTakenByTheBotWeak.contains(DistrictCard.PALACE));
        assertTrue(cardsDontTakenByTheBotWeak.contains(DistrictCard.MARKET));
        assertEquals(DistrictCard.TAVERN, cardTakenByTheBotWeak.get(0));
        assertEquals(3, cardsDontTakenByTheBotWeak.size());
        assertEquals(1, cardTakenByTheBotWeak.size());
    }

    @Test
    void testLibraryAndObservatory() {
        botWeak.getBoard().add(DistrictCard.LIBRARY);
        botWeak.getBoard().add(DistrictCard.OBSERVATORY);
        int oldHandSize = botWeak.getHands().size();

        botWeak.drawCard(cardsThatThePlayerDontWantAndThatThePlayerWant, DistrictCard.MARKET, DistrictCard.PALACE, DistrictCard.SCHOOL_OF_MAGIC);
        botWeak.getHands().addAll(cardsThatThePlayerDontWantAndThatThePlayerWant.get(DispatchState.CARDS_WANTED));

        //Verify that the hand size is correct
        assertEquals(oldHandSize + 2, botWeak.getHands().size());

        //Verify that there is 2 cards claimed and 1 card leaved
        assertEquals(2, cardsThatThePlayerDontWantAndThatThePlayerWant.get(DispatchState.CARDS_WANTED).size());
        assertEquals(1, cardsThatThePlayerDontWantAndThatThePlayerWant.get(DispatchState.CARDS_NOT_WANTED).size());

        assertTrue(botWeak.getHands().contains(DistrictCard.MARKET));
        assertTrue(botWeak.getHands().contains(DistrictCard.PALACE));
    }

    @Test
    void testWantToUseEffect() {
        assertTrue(botWeak.wantToUseEffect(true));
        assertFalse(botWeak.wantToUseEffect(false));
    }

    @Test
    void wantsToUseSmithyEffect() {
        botWeak.setGolds(4);
        assertFalse(botWeak.wantsToUseSmithyEffect());
        botWeak.setGolds(8);
        assertTrue(botWeak.wantsToUseSmithyEffect());
    }

    @Test
    void selectWhoWillBeAffectedByThiefEffect() {
        ArrayList<Player> players = new ArrayList<>();

        botWeak.setPlayerRole(CharacterCard.THIEF);
        Player player = new BotWeak();
        player.setGolds(31);
        player.setPlayerRole(CharacterCard.BISHOP);
        players.add(player);
        Player player2 = new BotWeak();
        player2.setGolds(32);
        player2.setPlayerRole(CharacterCard.MERCHANT);
        players.add(player2);

        ArrayList<CharacterCard> characterCards = new ArrayList<>();
        characterCards.add(CharacterCard.MAGICIAN);
        characterCards.add(CharacterCard.KING);
        characterCards.add(CharacterCard.BISHOP);
        characterCards.add(CharacterCard.MERCHANT);
        characterCards.add(CharacterCard.ARCHITECT);
        characterCards.add(CharacterCard.WARLORD);
        assertEquals(player2.getPlayerRole(), botWeak.selectWhoWillBeAffectedByThiefEffect(players, characterCards));
    }

    @Test
    void testHashCode() {
        assertEquals(botWeak.hashCode(), botWeak.hashCode());
        BotWeak botWeak2 = new BotWeak();
        assertNotEquals(botWeak.hashCode(), botWeak2.hashCode());
        BotRandom botRandom = new BotRandom();
        assertNotEquals(botWeak.hashCode(), botRandom.hashCode());
    }


    @Test
    void testChooseHandCardToDiscard() {
        BotWeak botWeak = new BotWeak();

        // Test when hands is empty
        assertNull(botWeak.chooseHandCardToDiscard());

        // Test when no card in hands has DistrictValue >= 3
        botWeak.getHands().add(DistrictCard.MARKET); // Assuming MARKET has DistrictValue < 3
        assertNull(botWeak.chooseHandCardToDiscard());

        // Test when a card in hands has DistrictValue >= 3
        botWeak.getHands().add(DistrictCard.PALACE); // Assuming PALACE has DistrictValue >= 3
        assertEquals(DistrictCard.PALACE, botWeak.chooseHandCardToDiscard());
    }

    @Test
    void testChooseColorForHauntedCity(){
        botWeak.getBoard().add(DistrictCard.CASTLE);
        botWeak.getBoard().add(DistrictCard.MARKET);
        botWeak.getBoard().add(DistrictCard.SCHOOL_OF_MAGIC);
        botWeak.getBoard().add(DistrictCard.BATTLEFIELD);
        //botWeak don't have blue card in hand
        assertEquals(Color.BLUE, botWeak.chooseColorForHauntedCity());
        //botWeak has all colors in hand
        botWeak.getBoard().add(DistrictCard.CATHEDRAL);
        assertEquals(Color.PURPLE, botWeak.chooseColorForHauntedCity());
    }

    @Test
    void testWantToUseLaboratoryEffect(){
        botWeak.getHands().add(DistrictCard.MARKET);
        assertFalse(botWeak.wantToUseLaboratoryEffect());
        botWeak.getHands().add(DistrictCard.CASTLE);
        assertTrue(botWeak.wantToUseLaboratoryEffect());
    }

    @Test
    void testWhichWarlordEffect(){
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Player player = new BotWeak();
            player.setGolds(4);
            players.add(player);
        }
        //no-one has a 1 gold district
        assertEquals(DispatchState.EARNDISTRICT_WARLORD, botWeak.whichWarlordEffect(players));
        players.get(1).getBoard().add(DistrictCard.TAVERN);
        //someone has a 1 gold district
        assertEquals(DispatchState.DESTROY, botWeak.whichWarlordEffect(players));
    }

    @Test
    void testWhichMagicianEffect(){
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Player player = new BotRandom();
            player.setGolds(4);
            players.add(player);
        }
        //botWeak has the most cards
        botWeak.getHands().add(DistrictCard.TAVERN);
        players.add(botWeak);
        assertEquals(DispatchState.EXCHANGE_DECK, botWeak.whichMagicianEffect(players));
        //botWeak and a bot has the most cards
        players.get(1).getHands().add(DistrictCard.CASTLE);
        assertEquals(DispatchState.EXCHANGE_DECK, botWeak.whichMagicianEffect(players));
        //Someone has more cards than botWeak
        players.get(1).getHands().add(DistrictCard.FORTRESS);
        assertEquals(DispatchState.EXCHANGE_PLAYER, botWeak.whichMagicianEffect(players));
    }

    @Test
    void testChooseCardsToChange(){
        //botWeak don't have card who cost less than 3 gold
        botWeak.getHands().add(DistrictCard.TAVERN);
        assertTrue(botWeak.chooseCardsToChange().isEmpty());
        //botWeak have a card who cost more than 3 gold
        botWeak.getHands().add(DistrictCard.CASTLE);
        assertFalse(botWeak.chooseCardsToChange().isEmpty());
    }

    @Test
    void testSelectMagicianTarget(){
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Player player = new BotRandom();
            player.setGolds(4);
            players.add(player);
        }
        //player 1 has the most card
        players.get(0).getHands().add(DistrictCard.CASTLE);
        players.get(0).getHands().add(DistrictCard.CASTLE);
        players.get(1).getHands().add(DistrictCard.TAVERN);
        assertEquals(players.get(0), botWeak.selectMagicianTarget(players));
        //player 1 and player 0 has the most card so last player in players is returned
        players.get(1).getHands().add(DistrictCard.OBSERVATORY);
        assertEquals(players.get(1), botWeak.selectMagicianTarget(players));
    }

    @Test
    void testWantToUseGraveyardEffect(){
        assertTrue(botWeak.wantToUseGraveyardEffect());
    }

    @Test
    void testEquals() {
    BotWeak botWeak1 = new BotWeak();
    Player botWeak2 = new BotWeak();
    assertFalse(botWeak.equals(botWeak1));
    assertFalse(botWeak.equals(null));
    assertTrue(botWeak.equals(botWeak));
    assertFalse(botWeak.equals(botWeak2));
    }
}