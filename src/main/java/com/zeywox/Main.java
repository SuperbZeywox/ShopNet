package com.zeywox;


import com.zeywox.Service.ShopNetEngine;
import com.zeywox.model.input.Pair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Main {

    private static void printTestInfo(int testCase, Pair query, String file_path) {
        System.out.println("**************** test case: " + testCase + " ****************");
        System.out.println("input file path: " + file_path);
        System.out.println("query: " + query);
    }

    public static void main(String[] args) throws IOException {
        List<Pair> pairs = new ArrayList<>();
        pairs.add(new Pair("Tuesday", 7));
        pairs.add(new Pair("Friday", 8));

        for (int testCase = 1; testCase < 3; testCase++) {
            String file_path = String.format("data/test%d.json", testCase);
            Pair query = pairs.get(testCase - 1);
            printTestInfo(testCase, query, file_path);
            File file = new File(file_path);
            ShopNetEngine engine = new ShopNetEngine(query.getDay(), query.getMeetNode());
            engine.deserialize(file);
            System.out.println(engine.dijkstra());
        }

    }


}