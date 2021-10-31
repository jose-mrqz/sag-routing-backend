package pe.sag.routing.algorithm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Node {
    int x;
    int y;
    int idx;

    public int calculateDistance(Node n) {
        return Math.abs(x - n.x) + Math.abs(y - n.y);
    }
    public /*abstract*/ void reset(){};
}
