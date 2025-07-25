package com.zeywox.model;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.zeywox.model.graph.Edge;

import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.util.*;

/**
 * this is an abstract class for the Engine, it holds data and deserialization
 * while engine holds computations
 */

@AllArgsConstructor @NoArgsConstructor @Getter
public class ShopNet {

    protected static final int INF = Integer.MAX_VALUE / 2;
    protected String shopNetId; // hash of the json data
    protected int numberOfNodes;
    protected int totalProducts;
    protected List<Edge>[] adjacencyList;
    protected int[] bitMasks; // products each shop sells
    protected int[] dailyAvailabilityMask;   // products each shops sells on query day
    protected boolean[] openToday;    // computed per day, shops that are open today
    protected int meetNode = -1;
    protected int tdIndex; // target day index


    protected void init(int numberOfNodes) {
        this.numberOfNodes = numberOfNodes;
        adjacencyList = new ArrayList[numberOfNodes + 1];
        bitMasks = new int[numberOfNodes + 1];
        for (int i = 1; i <= numberOfNodes; i++) {
            adjacencyList[i] = new ArrayList<>();
        }
        dailyAvailabilityMask = new int[numberOfNodes + 1];
        openToday = new boolean[numberOfNodes + 1];
        if (meetNode == -1 || meetNode > numberOfNodes) meetNode = numberOfNodes;  // last node
    }

    private void deserializeEdges(JsonParser parser, int shopId) throws IOException {
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            int to = -1, cost = 0;
            // inside one neighbor object
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.currentToken() == JsonToken.FIELD_NAME) {
                    String currentName = parser.getCurrentName();
                    parser.nextToken();
                    switch (currentName) {
                        case "shop_id" -> to = parser.getIntValue();
                        case "road_cost" -> cost = parser.getIntValue();
                        default -> parser.skipChildren();
                    }
                }
            }
            adjacencyList[shopId].add(new Edge(to, cost));
            adjacencyList[to].add(new Edge(shopId, cost));
        }
    }

    private void deserializeProducts(JsonParser parser, int shopId) throws IOException {
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            int productId = parser.getIntValue();
            bitMasks[shopId] |= (1 << (productId - 1)); //
        }
    }

    private void deserializeShop(JsonParser parser) throws IOException {
        // inside shop
        int shopId = -1;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            if (parser.currentToken() == JsonToken.FIELD_NAME) {
                String shopFieldName = parser.getCurrentName();
                parser.nextToken(); // cases: [products,neighbors] : START_ARRAY
                switch (shopFieldName) {
                    case "id" -> shopId = parser.getIntValue();
                    case "products" -> deserializeProducts(parser, shopId);
                    case "workingDays" -> deserializeDays(parser, shopId);
                    case "neighbors" -> deserializeEdges(parser, shopId);
                    default -> parser.skipChildren();
                }
            }
        }
    }

    private void deserializeDays(JsonParser parser, int shopId) throws IOException {
        int mask = 0;
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            mask |= 1 << (DayOfWeek.valueOf(parser.getText().toUpperCase()).getValue() - 1);
        }
        dailyAvailabilityMask[shopId] |= mask;
    }

    @SuppressWarnings("unchecked")
    public void deserialize(File file) throws IOException {
        JsonFactory factory = new JsonFactory();
        try (JsonParser parser = factory.createParser(file)) {
            // inside top‐level object
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.currentToken() == JsonToken.FIELD_NAME) {
                    String currentName = parser.getCurrentName();
                    parser.nextToken(); // cases: START_OBJECT, but case shops it START_ARRAY
                    switch (currentName) {
                        case "total_shops" -> init(parser.getIntValue());
                        case "total_products" -> totalProducts = parser.getIntValue();
                        case "shopNetId" -> shopNetId = parser.getText();
                        case "shops" -> {
                            while (parser.nextToken() != JsonToken.END_ARRAY) {
                                deserializeShop(parser);
                            }
                        }
                        default -> parser.skipChildren();
                    }
                }
            }
        }
    }


}


