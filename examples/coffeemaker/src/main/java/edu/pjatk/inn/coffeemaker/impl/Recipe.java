package edu.pjatk.inn.coffeemaker.impl;

import sorcer.core.context.ServiceContext;
import sorcer.service.Context;
import sorcer.service.ContextException;

import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * Represents the recipe for a specific beverage.
 * The recipe contains the amount of ingredients (e.g. coffee and milk) necessary to prepare a beverage.
 *
 * @author Sarah and Mike
 */
public class Recipe implements Serializable {
    private String name;
    private int price;
    private int amtCoffee;
    private int amtMilk;
    private int amtSugar;
    private int amtChocolate;

    public Recipe() {
        this.name = "";
        this.price = 0;
        this.amtCoffee = 0;
        this.amtMilk = 0;
        this.amtSugar = 0;
        this.amtChocolate = 0;
    }

    /**
     * Returns amount of chocolate for this recipe.
     *
     * @return amount of chocolate
     */
    public int getAmtChocolate() {
        return amtChocolate;
    }

    /**
     * Sets amount of chocolate for this recipe.
     *
     * @param amtChocolate amount of chocolate
     */
    public void setAmtChocolate(int amtChocolate) {
        if (amtChocolate >= 0) {
            this.amtChocolate = amtChocolate;
        }
    }

    /**
     * Returns amount of coffee for this recipe.
     *
     * @return amount of coffee
     */
    public int getAmtCoffee() {
        return amtCoffee;
    }

    /**
     * Sets amount of coffee for this recipe.
     *
     * @param amtCoffee amount of coffee
     */
    public void setAmtCoffee(int amtCoffee) {
        if (amtCoffee >= 0) {
            this.amtCoffee = amtCoffee;
        }
    }

    /**
     * Returns amount of milk for this recipe.
     *
     * @return amount of milk
     */
    public int getAmtMilk() {
        return amtMilk;
    }

    /**
     * Sets amount of milk for this recipe.
     *
     * @param amtMilk amount of milk
     */
    public void setAmtMilk(int amtMilk) {
        if (amtMilk >= 0) {
            this.amtMilk = amtMilk;
        }
    }

    /**
     * Returns amount of sugar for this recipe.
     *
     * @return amount of sugar
     */
    public int getAmtSugar() {
        return amtSugar;
    }

    /**
     * Sets amount of sugar for this recipe.
     *
     * @param amtSugar amount of sugar
     */
    public void setAmtSugar(int amtSugar) {
        if (amtSugar >= 0) {
            this.amtSugar = amtSugar;
        }
    }

    /**
     * Returns name of this recipe.
     *
     * @return name of this recipe
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name for this recipe.
     *
     * @param name name for this recipe
     */
    public void setName(String name) {
        if (name != null) {
            this.name = name;
        }
    }

    /**
     * Returns price for a beverage made according to this recipe.
     *
     * @return price for the beverage
     */
    public int getPrice() {
        return price;
    }

    /**
     * Sets price for a beverage made according to this recipe.
     *
     * @param price for the beverage
     */
    public void setPrice(int price) {
        if (price >= 0) {
            this.price = price;
        }
    }

    /**
     * Compares this recipe to given one and returns a value indicating if it's the same recipe.
     *
     * @param r the recipe to compare with
     * @return true if both recipes are the same, false otherwise
     */
    public boolean equals(Recipe r) {
        if ((this.name).equals(r.getName())) {
            return true;
        }
        return false;
    }

    /**
     * Returns string representation of this recipe (currently it only contains name of the recipe)
     *
     * @return string representation of this recipe
     */
    public String toString() {
        return name;
    }

    /**
     * Returns a recipe from given context.
     *
     * @param context context to create Recipe from
     * @return new recipe that matches given context
     * @throws ContextException if recipe object couldn't be created from the given context
     */
    static public Recipe getRecipe(Context context) throws ContextException {
        Recipe r = new Recipe();
        try {
            r.name = (String) context.getValue("key");
            r.price = (int) context.getValue("price");
            r.amtCoffee = (int) context.getValue("amtCoffee");
            r.amtMilk = (int) context.getValue("amtMilk");
            r.amtSugar = (int) context.getValue("amtSugar");
            r.amtChocolate = (int) context.getValue("amtChocolate");
        } catch (RemoteException e) {
            throw new ContextException(e);
        }
        return r;
    }

    /**
     * Creates and returns context from given recipe.
     *
     * @param recipe recipe to create context from
     * @return context that matches given recipe
     * @throws ContextException if context for given recipe couldn't be created
     */
    static public Context getContext(Recipe recipe) throws ContextException {
        Context cxt = new ServiceContext();
        cxt.putValue("key", recipe.getName());
        cxt.putValue("price", recipe.getPrice());
        cxt.putValue("amtCoffee", recipe.getAmtCoffee());
        cxt.putValue("amtMilk", recipe.getAmtMilk());
        cxt.putValue("amtSugar", recipe.getAmtSugar());
        cxt.putValue("amtChocolate", recipe.getAmtChocolate());
        return cxt;
    }

}
