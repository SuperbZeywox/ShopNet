package com.zeywox.Service;

import com.zeywox.model.ShopNet;
import com.zeywox.model.graph.Result;
import lombok.Getter;
import com.zeywox.model.graph.Edge;
import com.zeywox.model.graph.State;

import java.time.DayOfWeek;
import java.util.*;

@Getter
public class ShopNetEngine extends ShopNet {

    private Result result;
    private int allAvailableProductsMask = 0;

    public ShopNetEngine(String day) {
        this.tdIndex = DayOfWeek.valueOf(day.toUpperCase()).getValue() - 1;
    }

    public ShopNetEngine(String day, int meetNode) {
        this.tdIndex = DayOfWeek.valueOf(day.toUpperCase()).getValue() - 1;
        this.meetNode = meetNode;
    }

    public void refreshOpenToday() {
        int dayBit = 1 << tdIndex;
        int n = dailyAvailabilityMask.length;
        allAvailableProductsMask = 0;
        for (int shopId = 1; shopId < n; shopId++) {
            openToday[shopId] = (dailyAvailabilityMask[shopId] & dayBit) != 0;
            if (openToday[shopId]) allAvailableProductsMask |= bitMasks[shopId];
        }
    }

    public Result dijkstra() {
        // we prepare availability and max possible products mask
        refreshOpenToday();

        int maxMask = 1 << totalProducts;
        int[][] dist = new int[numberOfNodes + 1][maxMask];
        int[][] parentNode = new int[numberOfNodes + 1][maxMask];
        int[][] parentMask = new int[numberOfNodes + 1][maxMask];
        for (int node = 1; node <= numberOfNodes; node++) {
            Arrays.fill(dist[node], INF);
        }

        // we start at shop 1
        final int initialMask = openToday[1] ? bitMasks[1] : 0;
        dist[1][initialMask] = 0;

        PriorityQueue<State> priorityQueue =
                new PriorityQueue<>(Comparator.comparingInt(s -> s.totalCost));
        priorityQueue.offer(new State(1, initialMask, 0));

        while (!priorityQueue.isEmpty()) {
            State currentState = priorityQueue.poll();
            int currentNode = currentState.node;
            int currentMask = currentState.bitmask;
            int currentCost = currentState.totalCost;

            if (currentCost > dist[currentNode][currentMask]) continue;

            for (Edge edge : adjacencyList[currentNode]) {
                int neighborNode = edge.to;
                int nextMask = currentMask;

                // we collect products only if shop is open today
                if (openToday[neighborNode]) nextMask |= bitMasks[neighborNode];

                int nextCost = currentCost + edge.weight;
                if (nextCost < dist[neighborNode][nextMask]) {
                    dist[neighborNode][nextMask] = nextCost;
                    parentNode[neighborNode][nextMask] = currentNode;
                    parentMask[neighborNode][nextMask] = currentMask;
                    priorityQueue.offer(new State(neighborNode, nextMask, nextCost));
                }
            }
        }

        return minMeetingTime(dist, parentNode, parentMask, initialMask, maxMask);
    }

    private Result minMeetingTime(
            int[][] dist,
            int[][] parentNode,
            int[][] parentMask,
            int initialMask,
            int maxMask
    ) {
        // we split masks and meet at meetNode
        int bestCollectiveCost = INF;
        int bestMaskAgent1 = 0, bestMaskAgent2 = 0;
        for (int maskAgent1 = 0; maskAgent1 < maxMask; maskAgent1++) {
            for (int maskAgent2 = 0; maskAgent2 < maxMask; maskAgent2++) {
                if ((maskAgent1 | maskAgent2) == allAvailableProductsMask) {
                    int costAgent1 = dist[meetNode][maskAgent1];
                    int costAgent2 = dist[meetNode][maskAgent2];
                    int slowerArrival = Math.max(costAgent1, costAgent2);
                    if (slowerArrival < bestCollectiveCost) {
                        bestCollectiveCost = slowerArrival;
                        bestMaskAgent1 = maskAgent1;
                        bestMaskAgent2 = maskAgent2;
                    }
                }
            }
        }

        // we reconstruct each agent’s route
        List<Integer> routeAgent1 = buildPath(meetNode, bestMaskAgent1, initialMask, parentNode, parentMask);
        List<Integer> routeAgent2 = buildPath(meetNode, bestMaskAgent2, initialMask, parentNode, parentMask);
        result = new Result(bestCollectiveCost, routeAgent1, routeAgent2);
        return result;
    }


    /**
     * Backtracks from (endNode, endMask) to (1, initialMask) via parent arrays.
     */

    private List<Integer> buildPath(int endNode, int endMask, int initialMask,
                                    int[][] parentNode, int[][] parentMask) {
        LinkedList<Integer> path = new LinkedList<>();
        int currentNode = endNode;
        int currentMask = endMask;
        while (true) {
            path.addFirst(currentNode);
            if (currentNode == 1 && currentMask == initialMask) break;
            int previousMask = parentMask[currentNode][currentMask];
            int previousNode = parentNode[currentNode][currentMask];
            currentNode = previousNode;
            currentMask = previousMask;
        }
        return new ArrayList<>(path);
    }


}
