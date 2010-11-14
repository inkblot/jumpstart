package org.movealong.jumpstart.deploy;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: Oct 8, 2010
 * Time: 2:57:32 AM
 */
public abstract class DeploymentAdapter implements DeploymentListener {
    private Status status = Status.UNDEPLOYED;

    @Override
    public Status getStatus() {
        return status;
    }

    protected final void setStatus(Status status) {
        this.status = status;
    }

    protected final void nextStatus(Status nextStatus) {
        if (Status.values()[(getStatus().ordinal() + 1) % 4] == nextStatus) {
            setStatus(nextStatus);
        } else {
            throw new IllegalStateException("Cannot enter " + nextStatus + " state while listener is " + getStatus());
        }
    }

    public final void deploy() {
        nextStatus(Status.DEPLOYING);
        try {
            doDeploy();
            nextStatus(Status.DEPLOYED);
        } catch (RuntimeException e) {
            setStatus(Status.ERROR);
            throw e;
        }
    }

    protected abstract void doDeploy();

    public final void undeploy() {
        nextStatus(Status.UNDEPLOYING);
        try {
            doUndeploy();
            nextStatus(Status.UNDEPLOYED);
        } catch (RuntimeException e) {
            setStatus(Status.ERROR);
            throw e;
        }
    }

    protected abstract void doUndeploy();
}
