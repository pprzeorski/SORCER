package edu.pjatk.inn.coffeemaker;

import sorcer.service.Context;
import sorcer.service.ContextException;

import java.rmi.RemoteException;

public interface CaloriesCounter {

    Context countCalories(Context context) throws RemoteException, ContextException;

}
