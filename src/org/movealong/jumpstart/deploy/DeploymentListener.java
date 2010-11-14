package org.movealong.jumpstart.deploy;

import java.util.EventListener;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: Oct 8, 2010
 * Time: 1:47:34 AM
 */
public interface DeploymentListener extends EventListener {

    enum Status {
        UNDEPLOYED,
        DEPLOYING,
        DEPLOYED,
        UNDEPLOYING,

        ERROR
    }

    Status getStatus();

    void deploy();

    void undeploy();

}
