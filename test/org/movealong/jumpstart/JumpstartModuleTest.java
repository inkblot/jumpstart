package org.movealong.jumpstart;

import com.google.inject.*;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.movealong.jumpstart.deploy.Deployable;
import org.movealong.jumpstart.deploy.AbstractDeployable;
import org.movealong.jumpstart.deploy.Dispatcher;
import org.movealong.jumpstart.deploy.JumpstartModule;

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: Apr 4, 2011
 * Time: 7:26:02 PM
 */
public class JumpstartModuleTest {
    private static final Set<Deployable> boo = null;

    private Mockery mock;
    private Deployable listener1;

    @Before
    public void setUp() {
        mock = new JUnit4Mockery();
        listener1 = mock.mock(Deployable.class, "listener1");
    }

    @After
    public void tearDown() {
        mock = null;
        listener1 = null;
    }

    @Test
    public void jumpstartBindings() throws Exception {
        Injector injector = Guice.createInjector(
                new JumpstartModule(),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        Jumpstart.addDeployable(binder(), listener1);
                        Jumpstart.addDeployable(binder(), TestDeployable.class);
                    }
                }
        );

        Key<Set<Deployable>> key = (Key<Set<Deployable>>) Key.get(getClass().getDeclaredField("boo").getGenericType());
        Set<Deployable> listeners = injector.getInstance(key);
        assertThat(listeners,
                allOf(
                        hasItem(listener1),
                        hasItem(instanceOf(TestDeployable.class))));
        assertThat(listeners.size(), equalTo(2));

        Deployable dispatcher = injector.getInstance(Key.get(Deployable.class, Dispatcher.class));
        assertThat(dispatcher, not(nullValue()));
    }

    public static class TestDeployable extends AbstractDeployable {
        @Override
        protected void doDeploy() {
        }

        @Override
        protected void doUndeploy() {
        }
    }
}
