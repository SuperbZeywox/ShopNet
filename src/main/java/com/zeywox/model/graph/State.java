package com.zeywox.model.graph;

public class State {

    public int node, bitmask, totalCost;

    public State(int node, int bitmask, int totalCost) {
        this.node = node;
        this.bitmask = bitmask;
        this.totalCost = totalCost;
    }


}
