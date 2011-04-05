package org.movealong.jumpstart.deploy;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: Oct 8, 2010
 * Time: 2:41:53 AM
 */
@RunWith(JMock.class)
public class DeploymentDispatcherTest {

    private Mockery mock;
    private JumpstartDispatcher dispatcher;
    private Deployable neverUsed;

    @Before
    public void setUp() throws Exception {
        mock = new JUnit4Mockery();
        neverUsed = mock.mock(Deployable.class, "neverUsed");
        mock.checking(new Expectations() {{
            allowing(neverUsed).getStatus(); will(returnValue(Deployable.Status.UNDEPLOYED));
        }});
    }

    @After
    public void tearDown() throws Exception {
        mock = null;
        dispatcher = null;
    }

    @Test
    public void testEmptyListeners() {
        dispatcher = new JumpstartDispatcher(Collections.<Deployable>emptySet());
        assertEquals(Deployable.Status.UNDEPLOYED, dispatcher.getStatus());
        dispatcher.deploy();
        assertEquals(Deployable.Status.DEPLOYED, dispatcher.getStatus());
        dispatcher.undeploy();
        assertEquals(Deployable.Status.UNDEPLOYED, dispatcher.getStatus());
    }

    @Test
    public void testNormalListeners() {
        Deployable listener1 = new NormalDeployable();
        Deployable listener2 = new NormalDeployable();
        dispatcher = new JumpstartDispatcher(new LinkedHashSet<Deployable>(Arrays.asList(
                listener1,
                listener2
        )));
        dispatcher.deploy();
        assertEquals(Deployable.Status.DEPLOYED, listener1.getStatus());
        assertEquals(Deployable.Status.DEPLOYED, listener2.getStatus());
        dispatcher.undeploy();
        assertEquals(Deployable.Status.UNDEPLOYED, listener1.getStatus());
        assertEquals(Deployable.Status.UNDEPLOYED, listener2.getStatus());
    }

    @Test
    public void testBrokenDeployment() {
        NormalDeployable normal1 = new NormalDeployable();
        BrokenDeployable broken = new BrokenDeployable();
        dispatcher = new JumpstartDispatcher(new LinkedHashSet<Deployable>(Arrays.asList(
                normal1,
                broken,
                neverUsed
        )));
        try {
            dispatcher.deploy();
            fail("Deploy should have thrown an exception");
        } catch (RuntimeException e) {
            // ok
        }
        assertEquals(Deployable.Status.UNDEPLOYED, normal1.getStatus());
        assertEquals(Deployable.Status.ERROR, broken.getStatus());
        assertEquals(Deployable.Status.ERROR, dispatcher.getStatus());
    }

    @Test
    public void testBrokenUndeployment() {
        Deployable normal1 = new NormalDeployable();
        Deployable broken = new BrokenUndeployable();
        Deployable normal2 = new NormalDeployable();
        dispatcher = new JumpstartDispatcher(new LinkedHashSet<Deployable>(Arrays.asList(
                normal1,
                broken,
                normal2
        )));
        dispatcher.deploy();
        assertEquals(Deployable.Status.DEPLOYED, normal1.getStatus());
        assertEquals(Deployable.Status.DEPLOYED, broken.getStatus());
        assertEquals(Deployable.Status.DEPLOYED, normal2.getStatus());
        assertEquals(Deployable.Status.DEPLOYED, dispatcher.getStatus());
        try {
            dispatcher.undeploy();
            fail("Undeploy should have thrown an exception");
        } catch (RuntimeException e) {
            // ok
        }
        assertEquals(Deployable.Status.UNDEPLOYED, normal1.getStatus());
        assertEquals(Deployable.Status.ERROR, broken.getStatus());
        assertEquals(Deployable.Status.UNDEPLOYED, normal2.getStatus());
        assertEquals(Deployable.Status.ERROR, dispatcher.getStatus());
    }

    @Test
    public void testTotallyBroken() throws Exception {
        Deployable brokenUndeploy = new BrokenUndeployable();
        Deployable brokenDeploy = new BrokenDeployable();
        dispatcher = new JumpstartDispatcher(new LinkedHashSet<Deployable>(Arrays.asList(
                brokenUndeploy,
                brokenDeploy,
                neverUsed
        )));
        try {
            dispatcher.deploy();
            fail("Deploy should have thrown an exception");
        } catch (RuntimeException e) {
            // ok
        }
        assertEquals(Deployable.Status.ERROR, brokenUndeploy.getStatus());
        assertEquals(Deployable.Status.ERROR, brokenDeploy.getStatus());
        assertEquals(Deployable.Status.ERROR, dispatcher.getStatus());
    }

    private static class NormalDeployable extends AbstractDeployable {
        @Override
        protected void doDeploy() {
        }

        @Override
        protected void doUndeploy() {
        }
    }

    private static class BrokenDeployable extends AbstractDeployable {
        @Override
        protected void doDeploy() {
            throw new RuntimeException("I'm broken!");
        }

        @Override
        protected void doUndeploy() {
        }
    }

    private static class BrokenUndeployable extends AbstractDeployable {
        @Override
        protected void doDeploy() {
        }

        @Override
        protected void doUndeploy() {
            throw new RuntimeException("I'm broken!");
        }
    }
}
