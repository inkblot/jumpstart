package org.movealong.jumpstart;

import com.google.inject.Binder;
import com.google.inject.multibindings.Multibinder;
import org.movealong.jumpstart.deploy.Deployable;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: Apr 4, 2011
 * Time: 7:21:51 PM
 */
public final class Jumpstart {
    public static void addDeployable(Binder binder, Deployable listener) {
        Multibinder.newSetBinder(binder, Deployable.class).addBinding().toInstance(listener);
    }

    public static void addDeployable(Binder binder, Class<? extends Deployable> listenerClass) {
        Multibinder.newSetBinder(binder, Deployable.class).addBinding().to(listenerClass);
    }
}
