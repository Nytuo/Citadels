package fr.cotedazur.univ.polytech.model.bot;

import fr.cotedazur.univ.polytech.logger.LamaLogger;
import fr.cotedazur.univ.polytech.model.card.CharacterCard;
import fr.cotedazur.univ.polytech.model.card.Color;
import fr.cotedazur.univ.polytech.model.card.DistrictCard;
import fr.cotedazur.univ.polytech.model.deck.Deck;
import fr.cotedazur.univ.polytech.model.deck.DeckFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.rmi.MarshalException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class BotRandomTest {
    @Mock
    Random random = mock(Random.class);

    Map<DispatchState, ArrayList<DistrictCard>> cardsThatThePlayerDontWantAndThatThePlayerWant = new EnumMap<>(DispatchState.class);

    BotRandom botRandom3;
    BotRandom botRandom2;
    BotRandom botRandom1;

    private Deck<DistrictCard> districtDeck;

    @BeforeEach
    void setUp() {
        LamaLogger.mute();
        botRandom1 = new BotRandom();
        botRandom2 = new BotRandom();
        botRandom3 = new BotRandom();
        botRandom1.setRandom(random);
        botRandom3.setRandom(random);
        cardsThatThePlayerDontWantAndThatThePlayerWant.put(DispatchState.CARDS_WANTED, new ArrayList<>());
        cardsThatThePlayerDontWantAndThatThePlayerWant.put(DispatchState.CARDS_NOT_WANTED, new ArrayList<>());
        this.districtDeck = DeckFactory.createDistrictDeck();
        this.districtDeck.shuffle();
    }

    @Test
    void testPutADistrict() {
        botRandom2.getHands().add(DistrictCard.TRADING_POST);
        botRandom2.setGolds(DistrictCard.TRADING_POST.getDistrictValue());
        assertNotNull(botRandom2.putADistrict());
        botRandom2.setGolds(DistrictCard.TRADING_POST.getDistrictValue());
        assertEquals(botRandom2.putADistrict(), botRandom2.getHands().get(0));
        botRandom2.getBoard().clear();

        botRandom2.drawCard(cardsThatThePlayerDontWantAndThatThePlayerWant, districtDeck.draw());
        botRandom2.setGolds(botRandom2.getHands().get(0).getDistrictValue());
        assertNotNull(botRandom2.putADistrict());

        botRandom2.getHands().clear();
        assertNull(botRandom2.putADistrict());
    }

    @Test
    void testChoiceToPutADistrictIfNoCardsInHand() {
        botRandom2.setPlayerRole(CharacterCard.ASSASSIN);
        assertNull(botRandom2.choiceHowToPlayDuringTheRound());
    }


    @Test
    void testBotRandomPutADistrict() {
        //Taking the third card from the hand of the random bot
        when(random.nextInt(anyInt())).thenReturn(0);
        botRandom1.setGolds(20); //add golds to be able to put a district
        botRandom1.drawCard(cardsThatThePlayerDontWantAndThatThePlayerWant, districtDeck.draw());
        botRandom1.drawCard(cardsThatThePlayerDontWantAndThatThePlayerWant, districtDeck.draw());
        botRandom1.drawCard(cardsThatThePlayerDontWantAndThatThePlayerWant, districtDeck.draw());
        botRandom1.drawCard(cardsThatThePlayerDontWantAndThatThePlayerWant, districtDeck.draw());

        //Store the card that will be drawn from the hand
        when(random.nextInt(anyInt())).thenReturn(2);
        botRandom1.getHands().addAll(cardsThatThePlayerDontWantAndThatThePlayerWant.get(DispatchState.CARDS_WANTED));
        DistrictCard districtCard = botRandom1.getHands().get(2);
        botRandom1.addCardToBoard(botRandom1.putADistrict());
        assertEquals(districtCard, botRandom1.getBoard().get(0));
    }

    @Test
    void testBotRandomCollect2golds() {
        //0 is when the random bot should take the golds
        when(random.nextInt(anyInt())).thenReturn(0);


        //the bot should have 2 golds added
        assertEquals(DispatchState.TWO_GOLDS, botRandom1.startChoice());
    }

    @Test
    void testBotRandomDrawCardFrom2() {
        //With 1 the bot Random will choose to draw a card
        when(random.nextInt(anyInt())).thenReturn(1);
        int oldHandSize = botRandom1.getHands().size();
        botRandom1.startChoice();


        botRandom1.drawCard(cardsThatThePlayerDontWantAndThatThePlayerWant, DistrictCard.MARKET, DistrictCard.PALACE);
        botRandom1.getHands().addAll(cardsThatThePlayerDontWantAndThatThePlayerWant.get(DispatchState.CARDS_WANTED));
        assertEquals(DistrictCard.PALACE, botRandom1.getHands().get(0));

        //Verify that the hand size is correct
        assertEquals(oldHandSize + 1, botRandom1.getHands().size());

        //Verify that the cards the players don't want are correct
        assertEquals(DistrictCard.MARKET, cardsThatThePlayerDontWantAndThatThePlayerWant.get(DispatchState.CARDS_NOT_WANTED).get(0));
    }

    @Test
    void testBotRandomDrawCardFrom3() {
        //With 1 the bot Random will choose to draw a card
        when(random.nextInt(anyInt())).thenReturn(1);
        int oldHandSize = botRandom1.getHands().size();
        botRandom1.startChoice();

        botRandom1.drawCard(cardsThatThePlayerDontWantAndThatThePlayerWant, DistrictCard.MARKET, DistrictCard.PALACE, DistrictCard.CASTLE);
        botRandom1.getHands().add(cardsThatThePlayerDontWantAndThatThePlayerWant.get(DispatchState.CARDS_WANTED).get(0));
        assertEquals(DistrictCard.PALACE, botRandom1.getHands().get(0));

        //Verify that the hand size is correct
        assertEquals(oldHandSize + 1, botRandom1.getHands().size());

        //Verify that the cards the players don't want are correct
        assertEquals(DistrictCard.MARKET, cardsThatThePlayerDontWantAndThatThePlayerWant.get(DispatchState.CARDS_NOT_WANTED).get(0));
        assertEquals(DistrictCard.CASTLE, cardsThatThePlayerDontWantAndThatThePlayerWant.get(DispatchState.CARDS_NOT_WANTED).get(1));
    }

    @Test
    void testLibrary() {
        botRandom2.getBoard().add(DistrictCard.LIBRARY);
        int oldHandSize = botRandom2.getHands().size();

        botRandom2.drawCard(cardsThatThePlayerDontWantAndThatThePlayerWant, DistrictCard.MARKET, DistrictCard.PALACE);
        botRandom2.getHands().addAll(cardsThatThePlayerDontWantAndThatThePlayerWant.get(DispatchState.CARDS_WANTED));

        //Verify that the hand size is correct
        assertEquals(oldHandSize + 2, botRandom2.getHands().size());

        //Verify that the cards the players want are correct
        assertTrue(botRandom2.getHands().contains(DistrictCard.MARKET));
        assertTrue(botRandom2.getHands().contains(DistrictCard.PALACE));
    }

    @Test
    void testLibraryWithOneCard() {
        botRandom2.getBoard().add(DistrictCard.LIBRARY);
        int oldHandSize = botRandom2.getHands().size();

        botRandom2.drawCard(cardsThatThePlayerDontWantAndThatThePlayerWant, DistrictCard.MARKET);
        botRandom2.getHands().addAll(cardsThatThePlayerDontWantAndThatThePlayerWant.get(DispatchState.CARDS_WANTED));

        //Verify that the hand size is correct
        assertEquals(oldHandSize + 1, botRandom2.getHands().size());

        //Verify that the cards the players want are correct
        assertTrue(botRandom2.getHands().contains(DistrictCard.MARKET));
    }

    @Test
    void testLibraryAndObservatory() {
        botRandom2.getBoard().add(DistrictCard.LIBRARY);
        botRandom2.getBoard().add(DistrictCard.OBSERVATORY);
        int oldHandSize = botRandom2.getHands().size();

        botRandom2.drawCard(cardsThatThePlayerDontWantAndThatThePlayerWant, DistrictCard.MARKET, DistrictCard.PALACE, DistrictCard.CASTLE);
        botRandom2.getHands().addAll(cardsThatThePlayerDontWantAndThatThePlayerWant.get(DispatchState.CARDS_WANTED));

        //Verify that the hand size is correct
        assertEquals(oldHandSize + 2, botRandom2.getHands().size());

        //Verify that there is 2 cards claimed and 1 card leaved
        assertEquals(2, cardsThatThePlayerDontWantAndThatThePlayerWant.get(DispatchState.CARDS_WANTED).size());
        assertEquals(1, cardsThatThePlayerDontWantAndThatThePlayerWant.get(DispatchState.CARDS_NOT_WANTED).size());
    }

    @Test
    void testChoiceToPutADistrict() {
        //Put a district for the first call of the fonction and choose the
        when(random.nextInt(anyInt())).thenReturn(0).thenReturn(0);

        botRandom1.setGolds(20); //add golds to be able to put a district

        botRandom1.drawCard(cardsThatThePlayerDontWantAndThatThePlayerWant, districtDeck.draw());
        botRandom1.drawCard(cardsThatThePlayerDontWantAndThatThePlayerWant, districtDeck.draw());
        botRandom1.getHands().addAll(cardsThatThePlayerDontWantAndThatThePlayerWant.get(DispatchState.CARDS_WANTED));
        botRandom1.setPlayerRole(CharacterCard.ASSASSIN);

        when(random.nextInt(anyInt())).thenReturn(0).thenReturn(1);
        //Take a card to test if the bot has chosen to put a district
        DistrictCard card = botRandom1.getHands().get(1);
        assertEquals(card, botRandom1.choiceHowToPlayDuringTheRound());
        botRandom1.addCardToBoard(card);
        botRandom1.addCardToBoard(botRandom1.getHands().get(0));

        //Test when there is no card in hand
        when(random.nextInt(anyInt())).thenReturn(0).thenReturn(1);
        assertNull(botRandom1.choiceHowToPlayDuringTheRound());

        //Test when bot choose to not put a district
        botRandom2.drawCard(cardsThatThePlayerDontWantAndThatThePlayerWant, districtDeck.draw());
        botRandom2.drawCard(cardsThatThePlayerDontWantAndThatThePlayerWant, districtDeck.draw());
        when(random.nextInt(anyInt())).thenReturn(1);
        assertNull(botRandom1.choiceHowToPlayDuringTheRound());
    }



    @Test
    void testChooseCharacter() {
        //Test with king
        when(random.nextInt(anyInt())).thenReturn(3);
        Deck<CharacterCard> characterDeck = DeckFactory.createCharacterDeck();
        int characterNumber = botRandom1.chooseCharacter(characterDeck.getCards());
        botRandom1.setPlayerRole(characterDeck.draw(characterNumber));
        assertEquals(CharacterCard.KING, botRandom1.getPlayerRole());

        //Test with warlord
        when(random.nextInt(anyInt())).thenReturn(6);
        characterNumber = botRandom1.chooseCharacter(characterDeck.getCards());
        botRandom1.setPlayerRole(characterDeck.draw(characterNumber));
        assertEquals(CharacterCard.WARLORD, botRandom1.getPlayerRole());

        //Test with assassin
        when(random.nextInt(anyInt())).thenReturn(0);
        characterNumber = botRandom1.chooseCharacter(characterDeck.getCards());
        botRandom1.setPlayerRole(characterDeck.draw(characterNumber));
        assertEquals(CharacterCard.ASSASSIN, botRandom1.getPlayerRole());

        //Test with bishop
        when(random.nextInt(anyInt())).thenReturn(2);
        characterNumber = botRandom1.chooseCharacter(characterDeck.getCards());
        botRandom1.setPlayerRole(characterDeck.draw(characterNumber));
        assertEquals(CharacterCard.BISHOP, botRandom1.getPlayerRole());

        //Test with architect
        when(random.nextInt(anyInt())).thenReturn(3);
        characterNumber = botRandom1.chooseCharacter(characterDeck.getCards());
        botRandom1.setPlayerRole(characterDeck.draw(characterNumber));
        assertEquals(CharacterCard.ARCHITECT, botRandom1.getPlayerRole());

        //Test with merchant
        when(random.nextInt(anyInt())).thenReturn(2);
        characterNumber = botRandom1.chooseCharacter(characterDeck.getCards());
        botRandom1.setPlayerRole(characterDeck.draw(characterNumber));
        assertEquals(CharacterCard.MERCHANT, botRandom1.getPlayerRole());

        //Test with magician
        when(random.nextInt(anyInt())).thenReturn(1);
        characterNumber = botRandom1.chooseCharacter(characterDeck.getCards());
        botRandom1.setPlayerRole(characterDeck.draw(characterNumber));
        assertEquals(CharacterCard.MAGICIAN, botRandom1.getPlayerRole());

        //Test with thief
        when(random.nextInt(anyInt())).thenReturn(0);
        characterNumber = botRandom1.chooseCharacter(characterDeck.getCards());
        botRandom1.setPlayerRole(characterDeck.draw(characterNumber));
        assertEquals(CharacterCard.THIEF, botRandom1.getPlayerRole());
    }

    @Test
    void testChoosePlayerToDestroyInEmptyList() {
        when(random.nextInt(anyInt())).thenReturn(0);
        assertNull(botRandom1.choosePlayerToDestroy(Collections.emptyList()));
    }

    @Test
    void testChooseDistrictToDestroy() {
        when(random.nextInt(anyInt())).thenReturn(0);
        botRandom2.addCardToBoard(DistrictCard.CASTLE);
        botRandom2.addCardToBoard(DistrictCard.PALACE);
        botRandom2.addCardToBoard(DistrictCard.MANOR);
        assertEquals(DistrictCard.CASTLE, botRandom1.chooseDistrictToDestroy(botRandom2, botRandom2.getBoard()));
    }

    @Test
    void testWantToUseEffect() {
        when(random.nextInt(anyInt())).thenReturn(0);
        assertTrue(botRandom1.wantToUseEffect(true));
        assertTrue(botRandom1.wantToUseEffect(false));
    }

    @Test
    void wantsToUseSmithyEffect() {
        when(random.nextInt(anyInt())).thenReturn(0);
        assertTrue(botRandom1.wantsToUseSmithyEffect());
    }

    @Test
    void testChooseUseGraveyardEffect() {
        when(random.nextInt(anyInt())).thenReturn(0);
        assertTrue(botRandom1.wantToUseGraveyardEffect());
    }

    @Test
    void chooseColorForSchoolOfMagic(){
        when(random.nextInt(anyInt())).thenReturn(0);
        assertEquals(Color.BLUE, botRandom1.chooseColorForSchoolOfMagic());
    }

    @Test
    void chooseColorForHauntedCity(){
        when(random.nextInt(anyInt())).thenReturn(0);
        assertEquals(Color.BLUE, botRandom1.chooseColorForHauntedCity());
    }

    @Test
    void wantToUseLaboratoryEffect(){
        when(random.nextInt(anyInt())).thenReturn(0);
        assertTrue(botRandom1.wantToUseLaboratoryEffect());
    }

    @Test
    void testChooseHandCardToDiscard(){
        when(random.nextInt(anyInt())).thenReturn(0);
        when(random.nextBoolean()).thenReturn(true);
        botRandom1.getHands().add(DistrictCard.MARKET);
        botRandom1.getHands().add(DistrictCard.BATTLEFIELD);
        assertEquals(DistrictCard.MARKET, botRandom1.chooseHandCardToDiscard());
        when(random.nextBoolean()).thenReturn(false);
        assertNull(botRandom1.chooseHandCardToDiscard());

    }

}