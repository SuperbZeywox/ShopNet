package com.zeywox.model.graph;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Edge {

    @JsonProperty("shop_id")
    public int to;
    @JsonProperty("road_cost")
    public int weight;



}
