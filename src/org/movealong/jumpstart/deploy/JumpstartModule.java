package org.movealong.jumpstart.deploy;

import com.google.inject.AbstractModule;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: Oct 8, 2010
 * Time: 1:49:18 AM
 */
public class JumpstartModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Deployable.class).annotatedWith(Dispatcher.class).to(JumpstartDispatcher.class);
    }
}
