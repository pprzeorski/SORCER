package edu.pjatk.inn.coffeemaker.impl;

import edu.pjatk.inn.coffeemaker.CaloriesCounter;
import sorcer.service.Context;
import sorcer.service.ContextException;

import java.rmi.RemoteException;

public class CaloriesCounterImpl implements CaloriesCounter {

    @Override
    @SuppressWarnings("unchecked")
    public Context countCalories(Context context) throws RemoteException, ContextException {
        Context recipeContext = (Context) context.getValue("recipe");
        if (recipeContext != null) {
            Recipe recipe = Recipe.getRecipe(recipeContext);
            int calories = recipe.getCaloriesCount();
            context.putValue("calories", calories);
        }

        return context;
    }

}
