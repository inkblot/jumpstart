package org.movealong.congrieve.deploy;

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
    private DeploymentDispatcher dispatcher;
    private DeploymentListener neverUsed;

    @Before
    public void setUp() throws Exception {
        mock = new JUnit4Mockery();
        neverUsed = mock.mock(DeploymentListener.class, "neverUsed");
        mock.checking(new Expectations() {{
            allowing(neverUsed).getStatus(); will(returnValue(DeploymentListener.Status.UNDEPLOYED));
        }});
    }

    @After
    public void tearDown() throws Exception {
        mock = null;
        dispatcher = null;
    }

    @Test
    public void testEmptyListeners() {
        dispatcher = new DeploymentDispatcher(Collections.<DeploymentListener>emptySet());
        assertEquals(DeploymentListener.Status.UNDEPLOYED, dispatcher.getStatus());
        dispatcher.deploy();
        assertEquals(DeploymentListener.Status.DEPLOYED, dispatcher.getStatus());
        dispatcher.undeploy();
        assertEquals(DeploymentListener.Status.UNDEPLOYED, dispatcher.getStatus());
    }

    @Test
    public void testNormalListeners() {
        DeploymentListener listener1 = new NormalListener();
        DeploymentListener listener2 = new NormalListener();
        dispatcher = new DeploymentDispatcher(new LinkedHashSet<DeploymentListener>(Arrays.asList(
                listener1,
                listener2
        )));
        dispatcher.deploy();
        assertEquals(DeploymentListener.Status.DEPLOYED, listener1.getStatus());
        assertEquals(DeploymentListener.Status.DEPLOYED, listener2.getStatus());
        dispatcher.undeploy();
        assertEquals(DeploymentListener.Status.UNDEPLOYED, listener1.getStatus());
        assertEquals(DeploymentListener.Status.UNDEPLOYED, listener2.getStatus());
    }

    @Test
    public void testBrokenDeployment() {
        NormalListener normal1 = new NormalListener();
        BrokenDeployListener broken = new BrokenDeployListener();
        dispatcher = new DeploymentDispatcher(new LinkedHashSet<DeploymentListener>(Arrays.asList(
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
        assertEquals(DeploymentListener.Status.UNDEPLOYED, normal1.getStatus());
        assertEquals(DeploymentListener.Status.ERROR, broken.getStatus());
        assertEquals(DeploymentListener.Status.ERROR, dispatcher.getStatus());
    }

    @Test
    public void testBrokenUndeployment() {
        DeploymentListener normal1 = new NormalListener();
        DeploymentListener broken = new BrokenUndeployListener();
        DeploymentListener normal2 = new NormalListener();
        dispatcher = new DeploymentDispatcher(new LinkedHashSet<DeploymentListener>(Arrays.asList(
                normal1,
                broken,
                normal2
        )));
        dispatcher.deploy();
        assertEquals(DeploymentListener.Status.DEPLOYED, normal1.getStatus());
        assertEquals(DeploymentListener.Status.DEPLOYED, broken.getStatus());
        assertEquals(DeploymentListener.Status.DEPLOYED, normal2.getStatus());
        assertEquals(DeploymentListener.Status.DEPLOYED, dispatcher.getStatus());
        try {
            dispatcher.undeploy();
            fail("Undeploy should have thrown an exception");
        } catch (RuntimeException e) {
            // ok
        }
        assertEquals(DeploymentListener.Status.UNDEPLOYED, normal1.getStatus());
        assertEquals(DeploymentListener.Status.ERROR, broken.getStatus());
        assertEquals(DeploymentListener.Status.UNDEPLOYED, normal2.getStatus());
        assertEquals(DeploymentListener.Status.ERROR, dispatcher.getStatus());
    }

    @Test
    public void testTotallyBroken() throws Exception {
        DeploymentListener brokenUndeploy = new BrokenUndeployListener();
        DeploymentListener brokenDeploy = new BrokenDeployListener();
        dispatcher = new DeploymentDispatcher(new LinkedHashSet<DeploymentListener>(Arrays.asList(
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
        assertEquals(DeploymentListener.Status.ERROR, brokenUndeploy.getStatus());
        assertEquals(DeploymentListener.Status.ERROR, brokenDeploy.getStatus());
        assertEquals(DeploymentListener.Status.ERROR, dispatcher.getStatus());
    }

    private static class NormalListener extends DeploymentAdapter {
        @Override
        protected void doDeploy() {
        }

        @Override
        protected void doUndeploy() {
        }
    }

    private static class BrokenDeployListener extends DeploymentAdapter {
        @Override
        protected void doDeploy() {
            throw new RuntimeException("I'm broken!");
        }

        @Override
        protected void doUndeploy() {
        }
    }

    private static class BrokenUndeployListener extends DeploymentAdapter {
        @Override
        protected void doDeploy() {
        }

        @Override
        protected void doUndeploy() {
            throw new RuntimeException("I'm broken!");
        }
    }
}
