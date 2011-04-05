package org.movealong.jumpstart.deploy;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: Oct 8, 2010
 * Time: 1:59:54 AM
 */
@Singleton
class JumpstartDispatcher extends AbstractDeployable {
    private static final Logger logger = LoggerFactory.getLogger(JumpstartDispatcher.class);
    private final Stack<Deployable> deployedListeners = new Stack<Deployable>();
    private final Stack<Deployable> undeployedListeners = new Stack<Deployable>();

    @Inject
    public JumpstartDispatcher(Set<Deployable> listeners) {
        for (Deployable listener: listeners) {
            undeployedListeners.insertElementAt(listener, 0);
        }
    }

    @Override
    public void doDeploy() {
        try {
            while (!undeployedListeners.isEmpty()) {
                Deployable listener = undeployedListeners.pop();
                try {
                    logger.info("Deploying listener: listener=" + listener);
                    listener.deploy();
                    logger.info("Listener deployed: listener=" + listener);
                    deployedListeners.push(listener);
                } catch (RuntimeException e) {
                    logger.error("Deployment failed for listener: listener=" + listener, e);
                    undeployedListeners.push(listener);
                    throw e;
                }
            }
        } catch (RuntimeException e) {
            doUndeploy();
            throw e;
        }
    }

    @Override
    public void doUndeploy() {
        boolean failed = false;
        while (!deployedListeners.isEmpty()) {
            Deployable listener = deployedListeners.pop();
            try {
                listener.undeploy();
            } catch (RuntimeException e) {
                failed |= true;
            } finally {
                undeployedListeners.push(listener);
            }
        }
        if (failed) {
            throw new DeploymentException("Could not undeploy all listeners");
        }
    }

}
