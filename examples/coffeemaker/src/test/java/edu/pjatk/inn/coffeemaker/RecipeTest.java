package edu.pjatk.inn.coffeemaker;

import edu.pjatk.inn.coffeemaker.impl.Recipe;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RecipeTest {

    private Recipe recipe;

    @Before
    public void setUp() {
        recipe = new Recipe();
        recipe.setName("macchiato");
        recipe.setPrice(40);
        recipe.setAmtCoffee(7);
        recipe.setAmtMilk(1);
        recipe.setAmtSugar(2);
        recipe.setAmtChocolate(0);
    }

    @Test
    public void testCountCalories() {
        assertEquals(47, recipe.getCaloriesCount());
    }

}
