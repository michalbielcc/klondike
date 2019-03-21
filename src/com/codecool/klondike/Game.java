package com.codecool.klondike;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.codecool.klondike.Pile.PileType;

public class Game extends Pane {

    private List<Card> deck = new ArrayList<>();

    private Pile stockPile;
    private Pile discardPile;
    private List<Pile> foundationPiles = FXCollections.observableArrayList();
    private List<Pile> tableauPiles = FXCollections.observableArrayList();

    private double dragStartX, dragStartY;
    private List<Card> draggedCards = FXCollections.observableArrayList();

    private static double STOCK_GAP = 0;
    private static double FOUNDATION_GAP = 0;
    private static double TABLEAU_GAP = 30;


    private EventHandler<MouseEvent> onMouseClickedHandler = e -> {
        Card card = (Card) e.getSource();
        PileType sourcePile = card.getContainingPile().getPileType();
        if (card.getContainingPile().getPileType() == Pile.PileType.STOCK) {
            card.moveToPile(discardPile);
            card.flip();
            card.setMouseTransparent(false);
            System.out.println("Placed " + card + " to the waste.");
            if (stockPile.getTopCard() == null) {
                refillStockFromDiscard();
            }
        } else if (sourcePile == Pile.PileType.TABLEAU && card.getContainingPile().getTopCard() == card) {
            if (card.isFaceDown()) 
                card.flip();
            
        } if (sourcePile != Pile.PileType.FOUNDATION && sourcePile != Pile.PileType.STOCK) {
            for (Pile pile : foundationPiles) {
                if (pile.getTopCard() == null && card.getRank() == 1) {
                    card.moveToPile(pile);
                    System.out.println("ace to foundation");
                } if (pile.getTopCard() != null && card.getRank() - pile.getTopCard().getRank() == 1 && Card.isSameSuit(card, pile.getTopCard())) {
                    card.moveToPile(pile);
                    System.out.println("card to foundation");
                } 
            }
        }
    };

    private EventHandler<MouseEvent> stockReverseCardsHandler = e -> {
        refillStockFromDiscard();
    };

    private EventHandler<MouseEvent> onMousePressedHandler = e -> {
        dragStartX = e.getSceneX();
        dragStartY = e.getSceneY();
    };

    private EventHandler<MouseEvent> onMouseDraggedHandler = e -> {
        Card card = (Card) e.getSource();
        Pile activePile = card.getContainingPile();
        if (activePile.getPileType() == Pile.PileType.STOCK)
            return;
        if (activePile.getPileType() == Pile.PileType.TABLEAU && card.isFaceDown())
            return;
        double offsetX = e.getSceneX() - dragStartX;
        double offsetY = e.getSceneY() - dragStartY;
        boolean moreThanOneCard = false;
        if (activePile.getPileType() == Pile.PileType.TABLEAU && card != activePile.getTopCard() && card.isFaceDown() == false)
            moreThanOneCard = true;
        draggedCards.clear();
        if (moreThanOneCard == true) {
            for (int index = activePile.getCards().indexOf(card); index < activePile.numOfCards(); index ++) { 
                draggedCards.add(activePile.getCards().get(index));
            }
        }
        if (moreThanOneCard == false) {
            draggedCards.add(card);
        }
        for (Card card1 : draggedCards) {
            card1.getDropShadow().setRadius(20);
            card1.getDropShadow().setOffsetX(10);
            card1.getDropShadow().setOffsetY(10);

            card1.toFront();
            card1.setTranslateX(offsetX);
            card1.setTranslateY(offsetY);
        }
        
    };

    private EventHandler<MouseEvent> onMouseReleasedHandler = e -> {
        if (draggedCards.isEmpty())
            return;
        Card card = (Card) e.getSource();
        Pile tableauPile = getValidIntersectingPile(card, tableauPiles);
        Pile foundationPile = getValidIntersectingPile(card, foundationPiles);
        Pile sourcePile = card.getContainingPile();
        //TODO
        if (tableauPile != null) {
            handleValidMove(card, tableauPile);
        } else if (foundationPile != null) {
            handleValidMove(card, foundationPile);
        } else {
            draggedCards.forEach(MouseUtil::slideBack);
            draggedCards = FXCollections.observableArrayList();
            draggedCards.clear();
        }
    };

    public boolean isGameWon() {
        //TODO
        return false;
    }

    public Game() {
        deck = Card.createNewDeck();
        initPiles();
        dealCards();
    }

    public void addMouseEventHandlers(Card card) {
        card.setOnMousePressed(onMousePressedHandler);
        card.setOnMouseDragged(onMouseDraggedHandler);
        card.setOnMouseReleased(onMouseReleasedHandler);
        card.setOnMouseClicked(onMouseClickedHandler);
    }

    public void refillStockFromDiscard() {
        //TODO
        Iterator<Card> discardedIterator = discardPile.getCards().iterator();
        while (discardPile.numOfCards() > 0) {
            Card discarded = discardedIterator.next();
            discarded.flip();
            discarded.moveToPile(stockPile);
        }
        System.out.println("Stock refilled from discard pile.");
    }

    public boolean isMoveValid(Card card, Pile destPile) {
        //TODO
        if (destPile.getPileType() == PileType.TABLEAU) {
            if (destPile.isEmpty() == false) {
                if (Card.isOppositeColor(card, destPile.getTopCard()) && Card.isSameSuit(card, destPile.getTopCard()) == false && destPile.getTopCard().getRank() - card.getRank() == 1) {
                    return true;
                }
            } else {
                if (card.getRank() == 13) {
                    return true;
                }
            }
            
        } else if (destPile.getPileType() == PileType.FOUNDATION) {
            if (destPile.isEmpty()) {
                if (card.getRank() == 1) {
                    System.out.println("placing ace on foundation");
                    return true;
                } 
            } else if (card.getRank() - destPile.getTopCard().getRank() == 1 && Card.isSameSuit(card, destPile.getTopCard())){
                return true;
            }
        }
        return false;

        // STOCK
        // DISCARD
        // FOUNDATION
        // TABLEAU
    }
    private Pile getValidIntersectingPile(Card card, List<Pile> piles) {
        Pile result = null;
        for (Pile pile : piles) {
            if (!pile.equals(card.getContainingPile()) &&
                    isOverPile(card, pile) &&
                    isMoveValid(card, pile))
                result = pile;
        }
        return result;
    }

    private boolean isOverPile(Card card, Pile pile) {
        if (pile.isEmpty())
            return card.getBoundsInParent().intersects(pile.getBoundsInParent());
        else
            return card.getBoundsInParent().intersects(pile.getTopCard().getBoundsInParent());
    }

    private void handleValidMove(Card card, Pile destPile) {
        String msg = null;
        if (destPile.isEmpty()) {
            if (destPile.getPileType().equals(Pile.PileType.FOUNDATION))
                msg = String.format("Placed %s to the foundation.", card);
            if (destPile.getPileType().equals(Pile.PileType.TABLEAU))
                msg = String.format("Placed %s to a new pile.", card);
        } else {
            msg = String.format("Placed %s to %s.", card, destPile.getTopCard());
        }
        System.out.println(msg);
        MouseUtil.slideToDest(draggedCards, destPile);
        draggedCards.clear();
    }


    private void initPiles() {
        stockPile = new Pile(Pile.PileType.STOCK, "Stock", STOCK_GAP);
        stockPile.setBlurredBackground();
        stockPile.setLayoutX(95);
        stockPile.setLayoutY(20);
        stockPile.setOnMouseClicked(stockReverseCardsHandler);
        getChildren().add(stockPile);

        discardPile = new Pile(Pile.PileType.DISCARD, "Discard", STOCK_GAP);
        discardPile.setBlurredBackground();
        discardPile.setLayoutX(285);
        discardPile.setLayoutY(20);
        getChildren().add(discardPile);

        for (int i = 0; i < 4; i++) {
            Pile foundationPile = new Pile(Pile.PileType.FOUNDATION, "Foundation " + i, FOUNDATION_GAP);
            foundationPile.setBlurredBackground();
            foundationPile.setLayoutX(610 + i * 180);
            foundationPile.setLayoutY(20);
            foundationPiles.add(foundationPile);
            getChildren().add(foundationPile);
        }
        for (int i = 0; i < 7; i++) {
            Pile tableauPile = new Pile(Pile.PileType.TABLEAU, "Tableau " + i, TABLEAU_GAP);
            tableauPile.setBlurredBackground();
            tableauPile.setLayoutX(95 + i * 180);
            tableauPile.setLayoutY(275);
            tableauPiles.add(tableauPile);
            getChildren().add(tableauPile);
        }
    }

    public void dealCards() {
        Iterator<Card> deckIterator = deck.iterator();
        for (int index = 6; index >= 0; index --) {
            for (Integer idx = index + 1; idx > 0; idx --) {
                Card card = deckIterator.next();
                if (idx.equals(1)) {
                    card.flip();
                }
                deckIterator.remove();
                tableauPiles.get(index).addCard(card);
                addMouseEventHandlers(card);
                getChildren().add(card);  
            }
        }
        deckIterator.forEachRemaining(card -> {
            stockPile.addCard(card);
            addMouseEventHandlers(card);
            getChildren().add(card);
        });
    }

    public void setTableBackground(Image tableBackground) {
        setBackground(new Background(new BackgroundImage(tableBackground,
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
    }

}