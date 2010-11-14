package org.movealong.jumpstart.deploy;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.multibindings.Multibinder;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: Oct 8, 2010
 * Time: 1:49:18 AM
 */
public class DeploymentModule extends AbstractModule {

    public static void addListener(Binder binder, DeploymentListener listener) {
        Multibinder.newSetBinder(binder, DeploymentListener.class).addBinding().toInstance(listener);
    }

    @Override
    protected void configure() {
        bind(DeploymentListener.class).annotatedWith(Dispatcher.class).to(DeploymentDispatcher.class);
    }
}
