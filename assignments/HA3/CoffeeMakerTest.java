package edu.pjatk.inn.coffeemaker;

import edu.pjatk.inn.coffeemaker.impl.CoffeeMaker;
import edu.pjatk.inn.coffeemaker.impl.Inventory;
import edu.pjatk.inn.coffeemaker.impl.Recipe;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sorcer.test.ProjectContext;
import org.sorcer.test.SorcerTestRunner;
import sorcer.service.Exertion;

import static org.junit.Assert.*;
import static sorcer.eo.operator.*;
import static sorcer.so.operator.exec;

/**
 * @author Mike Sobolewski
 */
@RunWith(SorcerTestRunner.class)
@ProjectContext("examples/coffeemaker")
public class CoffeeMakerTest {
    private final static Logger logger = LoggerFactory.getLogger(CoffeeMakerTest.class);

    private CoffeeMaker coffeeMaker;
    private Inventory inventory;
    private Recipe espresso, mocha, macchiato, americano;

    @Before
    public void setUp() {
        coffeeMaker = new CoffeeMaker();
        inventory = coffeeMaker.checkInventory();

        espresso = new Recipe();
        espresso.setName("espresso");
        espresso.setPrice(50);
        espresso.setAmtCoffee(6);
        espresso.setAmtMilk(1);
        espresso.setAmtSugar(1);
        espresso.setAmtChocolate(0);

        mocha = new Recipe();
        mocha.setName("mocha");
        mocha.setPrice(100);
        mocha.setAmtCoffee(8);
        mocha.setAmtMilk(1);
        mocha.setAmtSugar(1);
        mocha.setAmtChocolate(2);

        macchiato = new Recipe();
        macchiato.setName("macchiato");
        macchiato.setPrice(40);
        macchiato.setAmtCoffee(7);
        macchiato.setAmtMilk(1);
        macchiato.setAmtSugar(2);
        macchiato.setAmtChocolate(0);

        americano = new Recipe();
        americano.setName("americano");
        americano.setPrice(40);
        americano.setAmtCoffee(7);
        americano.setAmtMilk(1);
        americano.setAmtSugar(2);
        americano.setAmtChocolate(0);
    }

    @Test
    public void testContextCoffee() {
        assertEquals(6, espresso.getAmtCoffee());
    }

    @Test
    public void testContextMilk() {
        assertEquals(1, espresso.getAmtMilk());
    }

    @Test
    public void addRecipe() {
        coffeeMaker.addRecipe(mocha);
        assertEquals(coffeeMaker.getRecipeForName("mocha").getName(), "mocha");
    }

    @Test
    public void addContextRecipe() throws Exception {
        coffeeMaker.addRecipe(Recipe.getContext(mocha));
        assertEquals(coffeeMaker.getRecipeForName("mocha").getName(), "mocha");
    }

    @Test
    public void addServiceRecipe() throws Exception {
        Exertion cmt = task(sig("addRecipe", coffeeMaker),
                context(types(Recipe.class), args(espresso),
                        result("recipe/added")));

        logger.info("isAdded: " + exec(cmt));
        assertEquals(coffeeMaker.getRecipeForName("espresso").getName(), "espresso");
    }

    @Test
    public void addRecipes() {
        coffeeMaker.addRecipe(mocha);
        coffeeMaker.addRecipe(macchiato);
        coffeeMaker.addRecipe(americano);

        assertEquals(coffeeMaker.getRecipeForName("mocha").getName(), "mocha");
        assertEquals(coffeeMaker.getRecipeForName("macchiato").getName(), "macchiato");
        assertEquals(coffeeMaker.getRecipeForName("americano").getName(), "americano");
    }

    @Test
    public void makeCoffee() {
        coffeeMaker.addRecipe(macchiato);
        assertEquals(coffeeMaker.makeCoffee(macchiato, 100), 60);
    }

    // new tests

    @Test
    public void testAddRecipe() {
        assertTrue(coffeeMaker.addRecipe(americano));
        assertEquals(americano, coffeeMaker.getRecipeForName(americano.getName()));

        assertFalse(coffeeMaker.addRecipe(americano));
    }

    @Test
    public void testDeleteRecipe() {
        coffeeMaker.addRecipe(espresso);
        assertTrue(coffeeMaker.deleteRecipe(espresso));
        assertNull(coffeeMaker.getRecipeForName(espresso.getName()));
    }

    @Test
    public void testEditRecipe() {
        coffeeMaker.addRecipe(espresso);
        assertEquals(coffeeMaker.getRecipeForName("espresso").getName(), "espresso");

        coffeeMaker.editRecipe(espresso, americano);

        assertNull(coffeeMaker.getRecipeForName("espresso"));
        assertEquals(coffeeMaker.getRecipeForName("americano").getName(), "americano");
    }

    @Test
    public void testAddInventory() {
        int initialCoffee = inventory.getCoffee();
        int initialMilk = inventory.getMilk();
        int initialSugar = inventory.getSugar();
        int initialChocolate = inventory.getChocolate();

        int coffee = 2;
        int milk = 5;
        int sugar = 9;
        int chocolate = 1;

        assertTrue(coffeeMaker.addInventory(coffee, milk, sugar, chocolate));
        assertEquals(inventory.getChocolate(), initialChocolate + chocolate);
        assertEquals(inventory.getCoffee(), initialCoffee + coffee);
        assertEquals(inventory.getMilk(), initialMilk + milk);
        assertEquals(inventory.getSugar(), initialSugar + sugar);
    }

    @Test
    public void testCheckInventory() {
        Inventory currentInventory = coffeeMaker.checkInventory();
        assertEquals(currentInventory.getCoffee(), 15);
        assertEquals(currentInventory.getMilk(), 15);
        assertEquals(currentInventory.getSugar(), 15);
        assertEquals(currentInventory.getChocolate(), 15);
    }

    @Test
    public void testPurchaseCoffee() {
        coffeeMaker.addRecipe(espresso);

        int initialCoffee = inventory.getCoffee();
        int initialMilk = inventory.getMilk();
        int initialSugar = inventory.getSugar();
        int initialChocolate = inventory.getChocolate();

        int change = coffeeMaker.makeCoffee(espresso, 100);

        assertEquals(50, change);

        assertEquals(initialCoffee - espresso.getAmtCoffee(), inventory.getCoffee());
        assertEquals(initialMilk - espresso.getAmtMilk(), inventory.getMilk());
        assertEquals(initialSugar - espresso.getAmtSugar(), inventory.getSugar());
        assertEquals(initialChocolate - espresso.getAmtChocolate(), inventory.getChocolate());
    }

}

