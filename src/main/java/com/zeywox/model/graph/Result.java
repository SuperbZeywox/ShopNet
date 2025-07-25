package com.zeywox.model.graph;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class Result {

    int cost;
    List<Integer> agent1Path;
    List<Integer> agent2Path;


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("cost: ").append(cost)
                .append("\nagent1_path: ").append(getPath(agent1Path))
                .append("\nagent2_path: ").append(getPath(agent2Path));
        return sb.toString();
    }

    StringBuilder getPath(List<Integer> agentPath) {
        StringBuilder path = new StringBuilder();
        agentPath.forEach(agent -> path.append(agent).append(" -> "));
        int len = path.length();
        if (len > 0) {
            for (int i = 1; i < 5; i++) {
                path.deleteCharAt(len - i);
            }
        }
        return path;
    }

}
