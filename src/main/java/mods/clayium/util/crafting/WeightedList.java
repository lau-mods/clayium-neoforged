package mods.clayium.util.crafting;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WeightedList<E> extends ArrayList<WeightedResult<E>> {
    public WeightedList() {
        super();
    }

    public boolean add(E result, int weight) {
        return this.add(new WeightedResult<>(result, weight));
    }

    public List<E> getResultList() {
        List list = new ArrayList();

        for (int i = 0; i < this.size(); i++) {
            list.add(this.get(i).result);
        }

        return list;
    }

    public int getWeightSum() {
        int sum = 0;

        for (int i = 0; i < this.size(); i++) {
            sum += this.get(i).weight;
        }

        return sum;
    }

    public E put(Random random) {
        int sum = this.getWeightSum();
        if (sum <= 0) {
            return this.get(random.nextInt(this.size())).result;
        } else {
            int pos = random.nextInt(sum);
            sum = 0;

            for (int i = 0; i < this.size(); i++) {
                sum += this.get(i).weight;
                if (sum > pos) {
                    return this.get(i).result;
                }
            }

            return null;
        }
    }
}
